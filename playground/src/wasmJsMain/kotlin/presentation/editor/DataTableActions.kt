package presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_editor_add_row
import chartsproject.playground.generated.resources.playground_editor_cancel
import chartsproject.playground.generated.resources.playground_editor_randomize
import chartsproject.playground.generated.resources.playground_editor_randomize_dialog_message
import chartsproject.playground.generated.resources.playground_editor_randomize_dialog_title
import chartsproject.playground.generated.resources.playground_editor_reset
import chartsproject.playground.generated.resources.playground_editor_reset_dialog_message
import chartsproject.playground.generated.resources.playground_editor_reset_dialog_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class ConfirmationAction(
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
internal fun TableActions(
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
internal fun ConfirmationDialog(
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
