package presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_nav_open_menu
import domain.ChartEditorState
import domain.EditorAction
import interop.copyTextToClipboard
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditorRoute(viewModel: EditorViewModel) {
    val state by viewModel.state.collectAsState()
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current

    EditorScreen(
        state = state,
        onAction = viewModel::dispatch,
        onCopyCode = { code -> copyTextToClipboard(clipboard, code) },
        onOpenUri = uriHandler::openUri,
    )
}

@Composable
fun EditorScreen(
    state: ChartEditorState,
    onAction: (EditorAction) -> Unit,
    onCopyCode: suspend (String) -> Boolean,
    onOpenUri: (String) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth >= EditorCompactLayoutBreakpoint) {
                Row(modifier = Modifier.fillMaxSize()) {
                    ChartTypeRail(
                        chartTypes = state.chartTypes,
                        selectedType = state.selectedChartType,
                        snapshotMetadata = state.snapshotMetadata,
                        onTypeSelected = { chartType -> onAction(EditorAction.SelectChart(chartType)) },
                        onOpenUri = onOpenUri,
                    )
                    ChartTypeRailDivider()
                    EditorContent(
                        state = state,
                        onAction = onAction,
                        onCopyCode = onCopyCode,
                        modifier = Modifier.weight(1f).padding(24.dp),
                    )
                }
            } else {
                CompactEditorScreen(
                    state = state,
                    onAction = onAction,
                    onCopyCode = onCopyCode,
                    onOpenUri = onOpenUri,
                )
            }
        }
    }
}

@Composable
private fun CompactEditorScreen(
    state: ChartEditorState,
    onAction: (EditorAction) -> Unit,
    onCopyCode: suspend (String) -> Boolean,
    onOpenUri: (String) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChartTypeDrawerContent(
                chartTypes = state.chartTypes,
                selectedType = state.selectedChartType,
                onTypeSelected = { chartType ->
                    onAction(EditorAction.SelectChart(chartType))
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(Res.string.playground_nav_open_menu),
                        )
                    }
                    PlaygroundBrand(modifier = Modifier.weight(1f))
                    Text(
                        text = state.selectedChartType.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BuildInfoButton(
                        snapshotMetadata = state.snapshotMetadata,
                        onOpenUri = onOpenUri,
                    )
                }
                EditorContent(
                    state = state,
                    onAction = onAction,
                    onCopyCode = onCopyCode,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

@Composable
private fun EditorContent(
    state: ChartEditorState,
    onAction: (EditorAction) -> Unit,
    onCopyCode: suspend (String) -> Boolean,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        EditorWorkspace(
            state = state,
            session = state.sessions.getValue(state.selectedChartType),
            chartType = state.selectedChartType,
            onAction = onAction,
            onCopyCode = onCopyCode,
            wideLayout = maxWidth >= EditorWideLayoutBreakpoint,
        )
    }
}
