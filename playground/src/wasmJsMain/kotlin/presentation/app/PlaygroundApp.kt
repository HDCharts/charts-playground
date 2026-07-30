package presentation.app

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import data.InMemoryEditorStore
import io.github.dautovicharis.charts.demoshared.theme.AppTheme
import io.github.dautovicharis.charts.demoshared.theme.docsSlate
import platform.snapshotPublishMetadata
import presentation.editor.EditorScreen
import presentation.editor.EditorViewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("Playground") {
        val viewModel =
            remember {
                EditorViewModel(
                    InMemoryEditorStore(snapshotMetadata = snapshotPublishMetadata()),
                )
            }

        AppTheme(
            theme = docsSlate,
            useDynamicColors = false,
        ) {
            EditorScreen(viewModel)
        }
    }
}
