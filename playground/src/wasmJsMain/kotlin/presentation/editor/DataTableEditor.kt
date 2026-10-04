package presentation.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_editor_add_row
import chartsproject.playground.generated.resources.playground_editor_cancel
import chartsproject.playground.generated.resources.playground_editor_delete_row_content_description
import chartsproject.playground.generated.resources.playground_editor_preview_unchanged
import chartsproject.playground.generated.resources.playground_editor_randomize
import chartsproject.playground.generated.resources.playground_editor_randomize_dialog_message
import chartsproject.playground.generated.resources.playground_editor_randomize_dialog_title
import chartsproject.playground.generated.resources.playground_editor_reset
import chartsproject.playground.generated.resources.playground_editor_reset_dialog_message
import chartsproject.playground.generated.resources.playground_editor_reset_dialog_title
import chartsproject.playground.generated.resources.playground_editor_row_number_header
import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.RowId
import domain.ValidationPath
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

// Width of one column weight unit; below this, data columns scroll horizontally instead of shrinking.
private val MinColumnUnitWidth = 88.dp
private val RowNumberColumnWidth = 42.dp
private val ActionColumnWidth = 56.dp
private val CellHeight = 56.dp
private const val ROW_HIGHLIGHT_MILLIS = 2200L

private val tableLineColor: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)

private enum class ConfirmationAction(
    val title: StringResource,
    val message: StringResource,
    val confirmLabel: StringResource,
) {
    RANDOMIZE(
        Res.string.playground_editor_randomize_dialog_title,
        Res.string.playground_editor_randomize_dialog_message,
        Res.string.playground_editor_randomize,
    ),
    RESET(
        Res.string.playground_editor_reset_dialog_title,
        Res.string.playground_editor_reset_dialog_message,
        Res.string.playground_editor_reset,
    ),
}

private class TableAction(
    val icon: ImageVector,
    val label: StringResource,
    val primary: Boolean,
    val onClick: () -> Unit,
)

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
    val highlightedRowId = rememberAppendedRowHighlight(dataTable.rows.map { row -> row.id })
    var confirmationAction by remember { mutableStateOf<ConfirmationAction?>(null) }
    val fillHeight = if (expandToFillHeight) Modifier.fillMaxHeight() else Modifier

    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().then(fillHeight).padding(16.dp)) {
            val totalColumnWeight = dataTable.columns.sumOf { column -> column.weight.toDouble() }.toFloat()
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
                TableHeader(
                    columns = dataTable.columns,
                    columnUnitWidth = columnUnitWidth,
                    columnsScroll = columnsScroll,
                )
                HorizontalDivider()

                val rowContent: @Composable (visualRowIndex: Int) -> Unit = { visualRowIndex ->
                    val row = visibleRows[visualRowIndex]
                    DataTableEditorRow(
                        row = row,
                        rowNumber = visibleRows.size - visualRowIndex,
                        containerColor =
                            tableRowContainerColor(
                                striped = visualRowIndex % 2 == 0,
                                invalid = row.id in invalidRowIds,
                                highlighted = row.id == highlightedRowId,
                            ),
                        columns = dataTable.columns,
                        invalidCellPaths = invalidCellPaths,
                        columnUnitWidth = columnUnitWidth,
                        columnsScroll = columnsScroll,
                        canDeleteRows = dataTable.rows.size > dataTable.minRows,
                        onCellChange = onCellChange,
                        onDeleteRow = onDeleteRow,
                    )
                }

                if (expandToFillHeight) {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                        items(
                            count = visibleRows.size,
                            key = { visualRowIndex -> visibleRows[visualRowIndex].id.value },
                        ) { visualRowIndex -> rowContent(visualRowIndex) }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
private fun TableActions(
    compact: Boolean,
    onAddRow: () -> Unit,
    onRandomize: () -> Unit,
    onReset: () -> Unit,
) {
    val actions =
        listOf(
            TableAction(Icons.Filled.Add, Res.string.playground_editor_add_row, primary = true, onClick = onAddRow),
            TableAction(
                Icons.Filled.Shuffle,
                Res.string.playground_editor_randomize,
                primary = false,
                onClick = onRandomize,
            ),
            TableAction(Icons.Filled.Refresh, Res.string.playground_editor_reset, primary = false, onClick = onReset),
        )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { action ->
            val label = stringResource(action.label)
            if (compact) {
                IconButton(onClick = action.onClick) {
                    Icon(imageVector = action.icon, contentDescription = label)
                }
            } else {
                Button(
                    onClick = action.onClick,
                    colors =
                        if (action.primary) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (compact) Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TableHeader(
    columns: List<DataTableColumn>,
    columnUnitWidth: Dp,
    columnsScroll: Modifier,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, tableLineColor),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(CellHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderCell(
                text = stringResource(Res.string.playground_editor_row_number_header),
                showDivider = true,
                modifier = Modifier.width(RowNumberColumnWidth),
                contentAlignment = Alignment.Center,
            )
            Row(modifier = Modifier.weight(1f).fillMaxHeight().then(columnsScroll)) {
                columns.forEachIndexed { index, column ->
                    HeaderCell(
                        text = column.label,
                        showDivider = index < columns.lastIndex,
                        modifier = Modifier.width(columnUnitWidth * column.weight),
                        contentAlignment = Alignment.CenterStart,
                    )
                }
            }
            Spacer(modifier = Modifier.width(ActionColumnWidth))
        }
    }
}

@Composable
private fun HeaderCell(
    text: String,
    showDivider: Boolean,
    modifier: Modifier,
    contentAlignment: Alignment,
) {
    Box(modifier = modifier.fillMaxHeight(), contentAlignment = contentAlignment) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
        if (showDivider) {
            VerticalDivider(color = tableLineColor, modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd))
        }
    }
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

@Composable
private fun ConfirmationDialog(
    action: ConfirmationAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(action.title)) },
        text = { Text(stringResource(action.message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(action.confirmLabel)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.playground_editor_cancel)) }
        },
    )
}

@Composable
private fun tableRowContainerColor(
    striped: Boolean,
    invalid: Boolean,
    highlighted: Boolean,
): Color =
    when {
        invalid -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.32f)
        highlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        striped -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f)
    }

@Composable
private fun DataTableEditorRow(
    row: DataTableRow,
    rowNumber: Int,
    containerColor: Color,
    columns: List<DataTableColumn>,
    invalidCellPaths: Set<ValidationPath>,
    columnUnitWidth: Dp,
    columnsScroll: Modifier,
    canDeleteRows: Boolean,
    onCellChange: (rowId: RowId, columnId: String, value: String) -> Unit,
    onDeleteRow: (rowId: RowId) -> Unit,
) {
    val cellBorder = BorderStroke(1.dp, tableLineColor)
    Row(
        modifier = Modifier.fillMaxWidth().height(CellHeight).background(containerColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(RowNumberColumnWidth).height(CellHeight).border(cellBorder),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rowNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(modifier = Modifier.weight(1f).then(columnsScroll)) {
            columns.forEach { column ->
                val isInvalidCell = ValidationPath(rowId = row.id, columnId = column.id) in invalidCellPaths
                Box(
                    modifier =
                        Modifier
                            .width(columnUnitWidth * column.weight)
                            .height(CellHeight)
                            .border(
                                if (isInvalidCell) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else cellBorder,
                            ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = row.cells[column.id].orEmpty(),
                        onValueChange = { nextValue -> onCellChange(row.id, column.id, nextValue) },
                        singleLine = true,
                        textStyle =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .semantics { contentDescription = "${column.label}, row $rowNumber" },
                    )
                }
            }
        }
        IconButton(
            onClick = { onDeleteRow(row.id) },
            enabled = canDeleteRows,
            modifier =
                Modifier
                    .width(ActionColumnWidth)
                    .height(CellHeight)
                    .alpha(if (canDeleteRows) 1f else 0.45f)
                    .border(cellBorder),
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(Res.string.playground_editor_delete_row_content_description),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
