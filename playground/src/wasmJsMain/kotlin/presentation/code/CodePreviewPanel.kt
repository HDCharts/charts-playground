package presentation.code

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_code_copied
import chartsproject.playground.generated.resources.playground_code_copy
import chartsproject.playground.generated.resources.playground_code_copy_failed
import chartsproject.playground.generated.resources.playground_code_full
import chartsproject.playground.generated.resources.playground_code_minimal
import chartsproject.playground.generated.resources.playground_code_minimal_compact
import chartsproject.playground.generated.resources.playground_code_title
import domain.CodegenMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private enum class CopyState {
    IDLE,
    COPIED,
    FAILED,
}

@Composable
fun CodePreviewPanel(
    code: String,
    mode: CodegenMode,
    onModeChange: (CodegenMode) -> Unit,
    onCopyCode: suspend (String) -> Boolean,
    expandToFillHeight: Boolean = false,
    showTitle: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var copyState by remember(code) { mutableStateOf(CopyState.IDLE) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(copyState) {
        if (copyState == CopyState.IDLE) return@LaunchedEffect
        delay(1000)
        copyState = CopyState.IDLE
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .let { base -> if (expandToFillHeight) base.fillMaxHeight() else base }
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showTitle) {
                Text(text = stringResource(Res.string.playground_code_title))
            }

            val (copyIcon, copyDescription) =
                when (copyState) {
                    CopyState.IDLE -> Icons.Filled.ContentCopy to stringResource(Res.string.playground_code_copy)
                    CopyState.COPIED -> Icons.Filled.Check to stringResource(Res.string.playground_code_copied)
                    CopyState.FAILED ->
                        Icons.Filled.ErrorOutline to
                            stringResource(Res.string.playground_code_copy_failed)
                }
            val copyCode = {
                coroutineScope.launch {
                    val success = onCopyCode(code)
                    copyState = if (success) CopyState.COPIED else CopyState.FAILED
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compactModeControls = maxWidth < 360.dp
                if (compactModeControls) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { onModeChange(CodegenMode.MINIMAL) },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                colors = codeModeButtonColors(mode == CodegenMode.MINIMAL),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(Res.string.playground_code_minimal_compact), maxLines = 1)
                            }
                            Button(
                                onClick = { onModeChange(CodegenMode.FULL) },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                colors = codeModeButtonColors(mode == CodegenMode.FULL),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(Res.string.playground_code_full), maxLines = 1)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            CopyCodeButton(
                                icon = copyIcon,
                                description = copyDescription,
                                copyState = copyState,
                                onClick = { copyCode() },
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { onModeChange(CodegenMode.MINIMAL) },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = codeModeButtonColors(mode == CodegenMode.MINIMAL),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(Res.string.playground_code_minimal), maxLines = 1)
                        }
                        Button(
                            onClick = { onModeChange(CodegenMode.FULL) },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = codeModeButtonColors(mode == CodegenMode.FULL),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(Res.string.playground_code_full), maxLines = 1)
                        }
                        CopyCodeButton(
                            icon = copyIcon,
                            description = copyDescription,
                            copyState = copyState,
                            onClick = { copyCode() },
                        )
                    }
                }
            }

            SelectionContainer(
                modifier =
                    if (expandToFillHeight) {
                        Modifier.fillMaxWidth().weight(1f)
                    } else {
                        Modifier.fillMaxWidth()
                    },
            ) {
                val styledCode = buildStyledCode(code, MaterialTheme.colorScheme)
                Text(
                    text = styledCode,
                    fontFamily = FontFamily.Monospace,
                    modifier =
                        if (expandToFillHeight) {
                            Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    min = 220.dp,
                                    max = 320.dp,
                                ).verticalScroll(rememberScrollState())
                        },
                )
            }
        }
    }
}

@Composable
private fun codeModeButtonColors(selected: Boolean) =
    if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

@Composable
private fun CopyCodeButton(
    icon: ImageVector,
    description: String,
    copyState: CopyState,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint =
                when (copyState) {
                    CopyState.COPIED -> MaterialTheme.colorScheme.primary
                    CopyState.FAILED -> MaterialTheme.colorScheme.error
                    CopyState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}
