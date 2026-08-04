package presentation.editor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
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
import domain.DataTableState
import domain.RowId
import domain.ValidationPath
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private enum class ConfirmationAction {
    RANDOMIZE,
    RESET,
}

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
    val canDeleteRows = dataTable.rows.size > dataTable.minRows
    val rowNumberColumnWidth = 42.dp
    val actionColumnWidth = 56.dp
    val cellHeight = 56.dp
    val currentRowIds = dataTable.rows.map { row -> row.id }
    val visibleRows = dataTable.rows.asReversed()
    var previousRowIds by remember { mutableStateOf(currentRowIds) }
    var highlightedRowId by remember { mutableStateOf<RowId?>(null) }
    var confirmationAction by remember { mutableStateOf<ConfirmationAction?>(null) }

    LaunchedEffect(currentRowIds) {
        val isAppendInsertion =
            currentRowIds.size == previousRowIds.size + 1 &&
                currentRowIds.dropLast(1) == previousRowIds
        if (isAppendInsertion) {
            highlightedRowId = currentRowIds.lastOrNull()
        }
        previousRowIds = currentRowIds
    }

    LaunchedEffect(highlightedRowId) {
        val targetId = highlightedRowId ?: return@LaunchedEffect
        delay(2200)
        if (highlightedRowId == targetId) {
            highlightedRowId = null
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        val panelModifier =
            Modifier
                .fillMaxWidth()
                .then(if (expandToFillHeight) Modifier.fillMaxHeight() else Modifier)
                .padding(16.dp)

        Column(modifier = panelModifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val showCompactActions = maxWidth < EditorCompactHeaderBreakpoint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showCompactActions) {
                        IconButton(onClick = onAddRow) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(Res.string.playground_editor_add_row),
                            )
                        }
                        IconButton(onClick = { confirmationAction = ConfirmationAction.RANDOMIZE }) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = stringResource(Res.string.playground_editor_randomize),
                            )
                        }
                        IconButton(onClick = { confirmationAction = ConfirmationAction.RESET }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(Res.string.playground_editor_reset),
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Button(
                            onClick = onAddRow,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = stringResource(Res.string.playground_editor_add_row),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Button(
                            onClick = { confirmationAction = ConfirmationAction.RANDOMIZE },
                            colors = ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = stringResource(Res.string.playground_editor_randomize),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Button(
                            onClick = { confirmationAction = ConfirmationAction.RESET },
                            colors = ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = stringResource(Res.string.playground_editor_reset),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(cellHeight),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(rowNumberColumnWidth).fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.playground_editor_row_number_header),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxHeight(),
                    )
                    dataTable.columns.forEachIndexed { index, column ->
                        Box(
                            modifier = Modifier.weight(column.weight).fillMaxHeight().padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = column.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (index < dataTable.columns.lastIndex) {
                            VerticalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                                modifier = Modifier.fillMaxHeight(),
                            )
                        }
                    }
                    Box(modifier = Modifier.width(actionColumnWidth).fillMaxHeight())
                }
            }
            HorizontalDivider()

            Column(
                modifier =
                    if (expandToFillHeight) {
                        Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                    } else {
                        Modifier.fillMaxWidth()
                    },
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                visibleRows.forEachIndexed { visualRowIndex, row ->
                    val rowIndex = dataTable.rows.lastIndex - visualRowIndex
                    val rowContainerColor =
                        when {
                            row.id in invalidRowIds ->
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.32f)
                            row.id == highlightedRowId ->
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            visualRowIndex % 2 == 0 -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f)
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.width(rowNumberColumnWidth),
                            color = rowContainerColor,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(cellHeight),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = (rowIndex + 1).toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        dataTable.columns.forEach { column ->
                            val isInvalidCell =
                                ValidationPath(rowId = row.id, columnId = column.id) in invalidCellPaths
                            Surface(
                                modifier = Modifier.weight(column.weight),
                                color = rowContainerColor,
                                border =
                                    BorderStroke(
                                        1.dp,
                                        if (isInvalidCell) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
                                        },
                                    ),
                            ) {
                                TextField(
                                    value = row.cells[column.id].orEmpty(),
                                    onValueChange = { nextValue ->
                                        onCellChange(row.id, column.id, nextValue)
                                    },
                                    singleLine = true,
                                    isError = isInvalidCell,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(cellHeight)
                                            .semantics {
                                                contentDescription = "${column.label}, row ${rowIndex + 1}"
                                            },
                                    colors =
                                        TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                        ),
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.width(actionColumnWidth),
                            color = rowContainerColor,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(cellHeight),
                                contentAlignment = Alignment.Center,
                            ) {
                                IconButton(
                                    onClick = { onDeleteRow(row.id) },
                                    enabled = canDeleteRows,
                                    modifier = Modifier.alpha(if (canDeleteRows) 1f else 0.45f),
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription =
                                            stringResource(Res.string.playground_editor_delete_row_content_description),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            validationMessage?.let { message ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = message,
                        color =
                            if (!validationIsBlocking) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (validationIsBlocking) {
                        Text(
                            text = stringResource(Res.string.playground_editor_preview_unchanged),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }

    confirmationAction?.let { action ->
        val isReset = action == ConfirmationAction.RESET
        AlertDialog(
            onDismissRequest = { confirmationAction = null },
            title = {
                Text(
                    stringResource(
                        if (isReset) {
                            Res.string.playground_editor_reset_dialog_title
                        } else {
                            Res.string.playground_editor_randomize_dialog_title
                        },
                    ),
                )
            },
            text = {
                Text(
                    stringResource(
                        if (isReset) {
                            Res.string.playground_editor_reset_dialog_message
                        } else {
                            Res.string.playground_editor_randomize_dialog_message
                        },
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmationAction = null
                        if (isReset) onReset() else onRandomize()
                    },
                ) {
                    Text(
                        stringResource(
                            if (isReset) {
                                Res.string.playground_editor_reset
                            } else {
                                Res.string.playground_editor_randomize
                            },
                        ),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmationAction = null }) {
                    Text(stringResource(Res.string.playground_editor_cancel))
                }
            },
        )
    }
}
