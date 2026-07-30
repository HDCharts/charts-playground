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
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.demoshared.theme.AppTheme
import io.github.dautovicharis.charts.demoshared.theme.docsSlate

@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val state by viewModel.state.collectAsState()
    val selectedSession = state.sessions.getValue(state.selectedChartType)

    AppTheme(
        theme = docsSlate,
        useDynamicColors = false,
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EditorHeader(
                    state = state,
                    selectedChartType = state.selectedChartType,
                    onAction = viewModel::dispatch,
                )

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    EditorWorkspace(
                        state = state,
                        session = selectedSession,
                        chartType = state.selectedChartType,
                        onAction = viewModel::dispatch,
                        wideLayout = maxWidth >= EditorWideLayoutBreakpoint,
                    )
                }
            }
        }
    }
}
