package presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import domain.ChartEditorState
import domain.EditorAction
import interop.copyTextToClipboard

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
    val selectedSession = state.sessions.getValue(state.selectedChartType)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorHeader(
                state = state,
                selectedChartType = state.selectedChartType,
                onAction = onAction,
                onOpenUri = onOpenUri,
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                EditorWorkspace(
                    state = state,
                    session = selectedSession,
                    chartType = state.selectedChartType,
                    onAction = onAction,
                    onCopyCode = onCopyCode,
                    wideLayout = maxWidth >= EditorWideLayoutBreakpoint,
                )
            }
        }
    }
}
