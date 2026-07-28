package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import chartsproject.charts_demo_shared.generated.resources.charts_logo
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_editor_add_row
import chartsproject.playground.generated.resources.playground_editor_delete_row_content_description
import chartsproject.playground.generated.resources.playground_editor_randomize
import chartsproject.playground.generated.resources.playground_editor_reset
import chartsproject.playground.generated.resources.playground_editor_row_number_header
import chartsproject.playground.generated.resources.playground_logo_content_description
import chartsproject.playground.generated.resources.playground_metadata_charts
import chartsproject.playground.generated.resources.playground_metadata_playground
import chartsproject.playground.generated.resources.playground_metadata_published
import chartsproject.playground.generated.resources.playground_title
import io.github.dautovicharis.charts.demoshared.startup.ChartsStartupGate
import io.github.dautovicharis.charts.demoshared.startup.StartupResources
import io.github.dautovicharis.charts.demoshared.startup.rememberStartupResourcesReady
import io.github.dautovicharis.charts.demoshared.theme.AppTheme
import io.github.dautovicharis.charts.demoshared.theme.docsSlate
import model.ChartType
import model.PlaygroundViewModel
import chartsproject.charts_demo_shared.generated.resources.Res as SharedRes

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("Playground") {
        val viewModel = remember { PlaygroundViewModel() }
        val state by viewModel.state.collectAsState()
        val resourcesReady = rememberPlaygroundStartupResourcesReady()

        AppTheme(
            theme = docsSlate,
            useDynamicColors = false,
        ) {
            ChartsStartupGate(resourcesReady) {
                PlaygroundScreen(viewModel)
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
                vectorDrawables = iconResources,
                strings =
                    listOf(
                        Res.string.playground_title,
                        Res.string.playground_logo_content_description,
                        Res.string.playground_editor_add_row,
                        Res.string.playground_editor_randomize,
                        Res.string.playground_editor_reset,
                        Res.string.playground_editor_row_number_header,
                        Res.string.playground_editor_delete_row_content_description,
                        Res.string.playground_metadata_charts,
                        Res.string.playground_metadata_playground,
                        Res.string.playground_metadata_published,
                    ),
            )
        }

    return rememberStartupResourcesReady(resources)
}
