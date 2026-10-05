package presentation.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_editor_delete_row_content_description
import chartsproject.playground.generated.resources.playground_editor_row_number_header
import domain.DataTableColumn
import domain.DataTableRow
import domain.RowId
import domain.ValidationPath
import org.jetbrains.compose.resources.stringResource

internal val RowNumberColumnWidth = 42.dp
internal val ActionColumnWidth = 56.dp
private val CellHeight = 56.dp

@Composable
internal fun TableHeader(
    columns: List<DataTableColumn>,
    columnUnitWidth: Dp,
    columnsScroll: Modifier,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
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
            VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
internal fun tableRowContainerColor(
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
internal fun DataTableEditorRow(
    row: DataTableRow,
    rowNumber: Int,
    containerColor: Color,
    columns: List<DataTableColumn>,
    invalidCellPaths: Set<ValidationPath>,
    selectedColumnId: String?,
    editing: Boolean,
    editStartText: String,
    columnUnitWidth: Dp,
    columnsScroll: Modifier,
    canDeleteRows: Boolean,
    onCellClick: (columnId: String) -> Unit,
    onCellChange: (rowId: RowId, columnId: String, value: String) -> Unit,
    onEditingFocusLost: (columnId: String) -> Unit,
    onDeleteRow: (rowId: RowId) -> Unit,
) {
    val cellBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
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
                val selected = column.id == selectedColumnId
                DataTableCell(
                    value = row.cells[column.id].orEmpty(),
                    editing = selected && editing,
                    editStartText = editStartText,
                    description = "${column.label}, row $rowNumber",
                    onClick = { onCellClick(column.id) },
                    onValueChange = { nextValue -> onCellChange(row.id, column.id, nextValue) },
                    onEditingFocusLost = { onEditingFocusLost(column.id) },
                    modifier =
                        Modifier
                            .width(columnUnitWidth * column.weight)
                            .height(CellHeight)
                            .border(
                                when {
                                    selected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    ValidationPath(rowId = row.id, columnId = column.id) in invalidCellPaths ->
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                    else -> cellBorder
                                },
                            ),
                )
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
                    .border(cellBorder)
                    .focusProperties { canFocus = false },
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(Res.string.playground_editor_delete_row_content_description),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun DataTableCell(
    value: String,
    editing: Boolean,
    editStartText: String,
    description: String,
    onClick: () -> Unit,
    onValueChange: (String) -> Unit,
    onEditingFocusLost: () -> Unit,
    modifier: Modifier,
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
    Box(
        modifier =
            modifier
                .focusProperties { canFocus = false }
                .clickable(interactionSource = null, indication = null, onClick = onClick)
                .padding(horizontal = 12.dp)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (editing) {
            var fieldValue by remember {
                mutableStateOf(
                    TextFieldValue(editStartText, TextRange(editStartText.length)),
                )
            }
            var focused by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }
            BasicTextField(
                value = fieldValue,
                onValueChange = { next ->
                    fieldValue = next
                    if (next.text != value) onValueChange(next.text)
                },
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (focused && !state.isFocused) onEditingFocusLost()
                            focused = state.isFocused
                        },
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        } else {
            Text(text = value, style = textStyle, maxLines = 1, softWrap = false)
        }
    }
}
