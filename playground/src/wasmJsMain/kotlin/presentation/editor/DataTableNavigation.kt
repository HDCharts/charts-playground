package presentation.editor

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import domain.DataTableColumn
import domain.DataTableRow
import domain.RowId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.focusComposeCanvas

private const val DELETE_CODE_POINT = 127

private val HandledKeys =
    setOf(
        Key.Enter,
        Key.NumPadEnter,
        Key.F2,
        Key.Escape,
        Key.Tab,
        Key.DirectionUp,
        Key.DirectionDown,
        Key.DirectionLeft,
        Key.DirectionRight,
        Key.Backspace,
        Key.Delete,
    )

internal data class TableCell(
    val rowId: RowId,
    val columnId: String,
)

// The table in on-screen order, with the edits a key press can make.
internal class NavigableTable(
    val rows: List<DataTableRow>,
    val columns: List<DataTableColumn>,
    val canDeleteRows: Boolean,
    val scrollable: Boolean,
    val onCellChange: (rowId: RowId, columnId: String, value: String) -> Unit,
    val onDeleteRow: (rowId: RowId) -> Unit,
) {
    fun contains(cell: TableCell): Boolean = rows.any { row -> row.id == cell.rowId }

    fun value(cell: TableCell): String = rows.first { row -> row.id == cell.rowId }.cells[cell.columnId].orEmpty()

    fun firstCell(): TableCell? {
        val row = rows.firstOrNull() ?: return null
        val column = columns.firstOrNull() ?: return null
        return TableCell(row.id, column.id)
    }
}

// Spreadsheet-style selection: one selected cell, which becomes a text field only while editing.
internal class DataTableNavigation(
    private val listState: LazyListState,
    private val scope: CoroutineScope,
) {
    val focusRequester = FocusRequester()

    var selected by mutableStateOf<TableCell?>(null)
        private set
    var editing by mutableStateOf(false)
        private set
    var editStartText by mutableStateOf("")
        private set
    var focused by mutableStateOf(false)
        private set
    private var originalText = ""

    fun selectedIn(table: NavigableTable): TableCell? = selected?.takeIf(table::contains)

    fun startEditing(
        table: NavigableTable,
        cell: TableCell,
        text: String = table.value(cell),
    ) {
        selected = cell
        editStartText = text
        originalText = table.value(cell)
        editing = true
    }

    // A cell that loses focus after another cell started editing must not stop that edit.
    fun onEditingFocusLost(cell: TableCell) {
        if (cell == selected) editing = false
    }

    fun onFocusChanged(
        table: NavigableTable,
        state: FocusState,
    ) {
        focused = state.hasFocus
        if (state.isFocused && selectedIn(table) == null) selected = table.firstCell()
    }

    fun onKey(
        event: KeyEvent,
        table: NavigableTable,
    ): Boolean {
        val current = selectedIn(table) ?: return false
        // Key up for a handled key is consumed too, so Escape does not reach the default handler that drops focus.
        if (event.type != KeyEventType.KeyDown) return event.type == KeyEventType.KeyUp && event.key in HandledKeys
        val tab = if (event.isShiftPressed) -1 else 1
        if (editing) {
            return when (event.key) {
                Key.Enter, Key.NumPadEnter, Key.DirectionDown -> finishEditing { move(table, current, rowDelta = 1) }
                Key.DirectionUp -> finishEditing { move(table, current, rowDelta = -1) }
                Key.Tab ->
                    finishEditing {
                        move(table, current, columnDelta = tab, wrap = true)
                        true
                    }
                Key.Escape -> {
                    table.onCellChange(current.rowId, current.columnId, originalText)
                    finishEditing { true }
                }
                else -> false
            }
        }
        return when {
            event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.F2 -> {
                startEditing(table, current)
                true
            }
            event.key == Key.DirectionUp -> move(table, current, rowDelta = -1)
            event.key == Key.DirectionDown -> move(table, current, rowDelta = 1)
            event.key == Key.DirectionLeft -> move(table, current, columnDelta = -1)
            event.key == Key.DirectionRight -> move(table, current, columnDelta = 1)
            event.key == Key.Tab -> move(table, current, columnDelta = tab, wrap = true)
            event.key == Key.Backspace && (event.isCtrlPressed || event.isMetaPressed) -> {
                if (table.canDeleteRows) table.onDeleteRow(current.rowId)
                true
            }
            event.key == Key.Backspace || event.key == Key.Delete -> {
                table.onCellChange(current.rowId, current.columnId, "")
                true
            }
            event.isTypedCharacter() -> {
                val text = event.utf16CodePoint.toChar().toString()
                startEditing(table, current, text)
                table.onCellChange(current.rowId, current.columnId, text)
                true
            }
            else -> false
        }
    }

    private fun finishEditing(then: () -> Boolean): Boolean {
        editing = false
        // After the field is gone, so its closing input session cannot take focus away again.
        scope.launch {
            withFrameNanos { }
            focusComposeCanvas()
            focusRequester.requestFocus()
        }
        return then()
    }

    // Arrows stop at the edges. Tab wraps to the next or previous row and returns false past either end,
    // so focus can leave the table.
    private fun move(
        table: NavigableTable,
        current: TableCell,
        rowDelta: Int = 0,
        columnDelta: Int = 0,
        wrap: Boolean = false,
    ): Boolean {
        val rows = table.rows
        val columns = table.columns
        val rowIndex = rows.indexOfFirst { row -> row.id == current.rowId }
        val columnIndex = columns.indexOfFirst { column -> column.id == current.columnId }
        val (targetRow, targetColumn) =
            if (wrap) {
                val linear = rowIndex * columns.size + columnIndex + columnDelta
                if (linear !in 0 until rows.size * columns.size) return false
                linear / columns.size to linear % columns.size
            } else {
                (rowIndex + rowDelta).coerceIn(rows.indices) to (columnIndex + columnDelta).coerceIn(columns.indices)
            }
        selected = TableCell(rows[targetRow].id, columns[targetColumn].id)
        if (table.scrollable) scrollToRow(targetRow)
        return true
    }

    private fun scrollToRow(rowIndex: Int) {
        val layout = listState.layoutInfo
        val first = layout.visibleItemsInfo.firstOrNull()?.index ?: return
        val last =
            layout.visibleItemsInfo.lastOrNull { item -> item.offset + item.size <= layout.viewportEndOffset }?.index
                ?: first
        when {
            rowIndex < first -> scope.launch { listState.scrollToItem(rowIndex) }
            rowIndex > last -> scope.launch { listState.scrollToItem(rowIndex - (last - first)) }
        }
    }
}

@Composable
internal fun rememberDataTableNavigation(listState: LazyListState): DataTableNavigation {
    val scope = rememberCoroutineScope()
    return remember(listState, scope) { DataTableNavigation(listState, scope) }
}

private fun KeyEvent.isTypedCharacter(): Boolean =
    !isCtrlPressed && !isMetaPressed && utf16CodePoint >= ' '.code && utf16CodePoint != DELETE_CODE_POINT
