package presentation.editor

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_editor_preview_unchanged
import domain.DataTableState
import domain.RowId
import domain.ValidationPath
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

// Width of one column weight unit; below this, data columns scroll horizontally instead of shrinking.
private val MinColumnUnitWidth = 88.dp
private const val ROW_HIGHLIGHT_MILLIS = 2200L

@Composable
fun DataTableEditor(
    dataTable: DataTableState,
    validationMessage: String?,
    validationIsBlocking: Boolean,
    invalidRowIds: Set<RowId>,
    invalidCellPaths: Set<ValidationPath>,
    onCellChange: (rowId: RowId, columnId: String, value: String) -> Unit,
    onAddRow: () -> Unit,
    onDeleteRow: (rowId: RowId) -> Unit,
    onRandomize: () -> Unit,
    onReset: () -> Unit,
    expandToFillHeight: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val visibleRows = dataTable.rows.asReversed()
    val columns = dataTable.columns
    val highlightedRowId = rememberAppendedRowHighlight(dataTable.rows.map { row -> row.id })
    var confirmationAction by remember { mutableStateOf<ConfirmationAction?>(null) }
    val fillHeight = if (expandToFillHeight) Modifier.fillMaxHeight() else Modifier
    val listState = rememberLazyListState()
    val navigation = rememberDataTableNavigation(listState)
    val table =
        NavigableTable(
            rows = visibleRows,
            columns = columns,
            canDeleteRows = dataTable.rows.size > dataTable.minRows,
            scrollable = expandToFillHeight,
            onCellChange = onCellChange,
            onDeleteRow = onDeleteRow,
        )
    val selected = navigation.selectedIn(table)

    LaunchedEffect(highlightedRowId) {
        val newRowId = highlightedRowId ?: return@LaunchedEffect
        val firstColumn = columns.firstOrNull() ?: return@LaunchedEffect
        navigation.startEditing(table, TableCell(newRowId, firstColumn.id))
        if (expandToFillHeight) listState.scrollToItem(0)
    }

    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().then(fillHeight).padding(16.dp)) {
            val totalColumnWeight = columns.sumOf { column -> column.weight.toDouble() }.toFloat()
            val fittedColumnUnitWidth = (maxWidth - RowNumberColumnWidth - ActionColumnWidth) / totalColumnWeight
            val columnsOverflow = fittedColumnUnitWidth < MinColumnUnitWidth
            val columnUnitWidth = fittedColumnUnitWidth.coerceAtLeast(MinColumnUnitWidth)
            val columnsScrollState = rememberScrollState()
            val columnsScroll = if (columnsOverflow) Modifier.horizontalScroll(columnsScrollState) else Modifier
            val compactActions = maxWidth < EditorCompactLayoutBreakpoint

            Column(
                modifier = Modifier.fillMaxWidth().then(fillHeight),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TableActions(
                    compact = compactActions,
                    onAddRow = onAddRow,
                    onRandomize = { confirmationAction = ConfirmationAction.RANDOMIZE },
                    onReset = { confirmationAction = ConfirmationAction.RESET },
                )
                TableHeader(columns = columns, columnUnitWidth = columnUnitWidth, columnsScroll = columnsScroll)
                HorizontalDivider()

                val rowContent: @Composable (visualRowIndex: Int) -> Unit = { visualRowIndex ->
                    val row = visibleRows[visualRowIndex]
                    val selectedColumnId =
                        selected
                            ?.takeIf { cell ->
                                cell.rowId == row.id && (navigation.focused || navigation.editing)
                            }?.columnId
                    DataTableEditorRow(
                        row = row,
                        rowNumber = visibleRows.size - visualRowIndex,
                        containerColor =
                            tableRowContainerColor(
                                striped = visualRowIndex % 2 == 0,
                                invalid = row.id in invalidRowIds,
                                highlighted = row.id == highlightedRowId,
                            ),
                        columns = columns,
                        invalidCellPaths = invalidCellPaths,
                        selectedColumnId = selectedColumnId,
                        editing = selectedColumnId != null && navigation.editing,
                        editStartText = navigation.editStartText,
                        columnUnitWidth = columnUnitWidth,
                        columnsScroll = columnsScroll,
                        canDeleteRows = table.canDeleteRows,
                        onCellClick = { columnId -> navigation.startEditing(table, TableCell(row.id, columnId)) },
                        onCellChange = onCellChange,
                        onEditingFocusLost = { columnId -> navigation.onEditingFocusLost(TableCell(row.id, columnId)) },
                        onDeleteRow = onDeleteRow,
                    )
                }

                val rowsModifier =
                    Modifier
                        .fillMaxWidth()
                        .onPreviewKeyEvent { event -> navigation.onKey(event, table) }
                        .onFocusChanged { state -> navigation.onFocusChanged(table, state) }
                        .focusRequester(navigation.focusRequester)
                        .focusable()
                if (expandToFillHeight) {
                    LazyColumn(state = listState, modifier = rowsModifier.weight(1f, fill = false)) {
                        items(
                            count = visibleRows.size,
                            key = { visualRowIndex -> visibleRows[visualRowIndex].id.value },
                        ) { visualRowIndex -> rowContent(visualRowIndex) }
                    }
                } else {
                    Column(modifier = rowsModifier) {
                        visibleRows.indices.forEach { visualRowIndex -> rowContent(visualRowIndex) }
                    }
                }

                if (columnsOverflow) {
                    HorizontalScrollbar(
                        adapter = rememberScrollbarAdapter(columnsScrollState),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = RowNumberColumnWidth, end = ActionColumnWidth, top = 4.dp),
                    )
                }

                validationMessage?.let { message -> ValidationMessage(message, blocking = validationIsBlocking) }
            }
        }
    }

    confirmationAction?.let { action ->
        ConfirmationDialog(
            action = action,
            onConfirm = {
                confirmationAction = null
                if (action == ConfirmationAction.RESET) onReset() else onRandomize()
            },
            onDismiss = { confirmationAction = null },
        )
    }
}

@Composable
private fun rememberAppendedRowHighlight(rowIds: List<RowId>): RowId? {
    var previousRowIds by remember { mutableStateOf(rowIds) }
    var highlightedRowId by remember { mutableStateOf<RowId?>(null) }

    LaunchedEffect(rowIds) {
        if (rowIds.size == previousRowIds.size + 1 && rowIds.dropLast(1) == previousRowIds) {
            highlightedRowId = rowIds.last()
        }
        previousRowIds = rowIds
    }

    LaunchedEffect(highlightedRowId) {
        if (highlightedRowId == null) return@LaunchedEffect
        delay(ROW_HIGHLIGHT_MILLIS.milliseconds)
        highlightedRowId = null
    }

    return highlightedRowId
}

@Composable
private fun ValidationMessage(
    message: String,
    blocking: Boolean,
) {
    val color = if (blocking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = message, color = color, style = MaterialTheme.typography.bodySmall)
        if (blocking) {
            Text(
                text = stringResource(Res.string.playground_editor_preview_unchanged),
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
