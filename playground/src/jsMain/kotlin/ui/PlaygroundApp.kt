package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import chartsproject.charts_demo_shared.generated.resources.charts_logo
import chartsproject.charts_demo_shared.generated.resources.ic_github
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_editor_add_row
import chartsproject.playground.generated.resources.playground_editor_delete_row_content_description
import chartsproject.playground.generated.resources.playground_editor_randomize
import chartsproject.playground.generated.resources.playground_editor_reset
import chartsproject.playground.generated.resources.playground_editor_row_number_header
import chartsproject.playground.generated.resources.playground_logo_content_description
import chartsproject.playground.generated.resources.playground_metadata
import chartsproject.playground.generated.resources.playground_metadata_unavailable
import chartsproject.playground.generated.resources.playground_open_github_content_description
import chartsproject.playground.generated.resources.playground_title
import io.github.dautovicharis.charts.demoshared.startup.ChartsStartupGate
import io.github.dautovicharis.charts.demoshared.startup.StartupResources
import io.github.dautovicharis.charts.demoshared.startup.rememberStartupResourcesReady
import io.github.dautovicharis.charts.demoshared.theme.AppTheme
import io.github.dautovicharis.charts.demoshared.theme.docsSlate
import model.ChartType
import model.PlaygroundAction
import model.PlaygroundViewModel
import org.jetbrains.skiko.wasm.onWasmReady
import chartsproject.charts_demo_shared.generated.resources.Res as SharedRes

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        ComposeViewport("Playground") {
            val viewModel = remember { PlaygroundViewModel() }
            val state by viewModel.state.collectAsState()
            val resourcesReady = rememberPlaygroundStartupResourcesReady()

            LaunchedEffect(Unit) {
                viewModel.dispatch(PlaygroundAction.LoadSnapshotMetadata)
            }

            AppTheme(
                theme = docsSlate,
                useDynamicColors = false,
            ) {
                ChartsStartupGate(resourcesReady && !state.snapshotMetadataLoading) {
                    PlaygroundScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun rememberPlaygroundStartupResourcesReady(): Boolean {
    val iconResources =
        remember {
            listOf(
                ChartType.PIE,
                ChartType.LINE,
                ChartType.MULTI_LINE,
                ChartType.BAR,
                ChartType.HISTOGRAM,
                ChartType.STACKED_BAR,
                ChartType.AREA,
                ChartType.RADAR,
            ).map(::chartTypeIconResource).distinct()
        }

    val resources =
        remember(iconResources) {
            StartupResources(
                bitmapDrawables = listOf(SharedRes.drawable.charts_logo),
                vectorDrawables = listOf(SharedRes.drawable.ic_github) + iconResources,
                strings =
                    listOf(
                        Res.string.playground_title,
                        Res.string.playground_logo_content_description,
                        Res.string.playground_open_github_content_description,
                        Res.string.playground_editor_add_row,
                        Res.string.playground_editor_randomize,
                        Res.string.playground_editor_reset,
                        Res.string.playground_editor_row_number_header,
                        Res.string.playground_editor_delete_row_content_description,
                        Res.string.playground_metadata,
                        Res.string.playground_metadata_unavailable,
                    ),
            )
        }

    return rememberStartupResourcesReady(resources)
}
