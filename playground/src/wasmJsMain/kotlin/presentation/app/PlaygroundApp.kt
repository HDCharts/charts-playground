package presentation.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_chart_title_label
import chartsproject.playground.generated.resources.playground_code_copy
import chartsproject.playground.generated.resources.playground_code_title
import chartsproject.playground.generated.resources.playground_editor_add_row
import chartsproject.playground.generated.resources.playground_editor_preview_unchanged
import chartsproject.playground.generated.resources.playground_editor_randomize
import chartsproject.playground.generated.resources.playground_editor_reset
import chartsproject.playground.generated.resources.playground_editor_row_number_header
import chartsproject.playground.generated.resources.playground_right_panel_code
import chartsproject.playground.generated.resources.playground_right_panel_settings
import chartsproject.playground.generated.resources.playground_style_color_default
import chartsproject.playground.generated.resources.playground_style_color_reset
import chartsproject.playground.generated.resources.playground_style_palette_custom
import chartsproject.playground.generated.resources.playground_style_palette_hint
import chartsproject.playground.generated.resources.playground_style_palette_item_label
import chartsproject.playground.generated.resources.playground_title
import data.InMemoryEditorStore
import domain.ChartType
import hdcharts.sample_shared.generated.resources.charts_logo
import io.github.hdcharts.sampleshared.startup.StartupGate
import io.github.hdcharts.sampleshared.startup.StartupResources
import io.github.hdcharts.sampleshared.theme.AppTheme
import io.github.hdcharts.sampleshared.theme.docsTheme
import platform.loadDarkThemePreference
import platform.saveDarkThemePreference
import platform.snapshotPublishMetadata
import presentation.editor.EditorRoute
import presentation.editor.EditorViewModel
import presentation.resources.chartTypeIconResource
import hdcharts.sample_shared.generated.resources.Res as SharedRes

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("Playground") {
        val viewModel =
            remember {
                EditorViewModel(
                    InMemoryEditorStore(snapshotMetadata = snapshotPublishMetadata()),
                )
            }

        val startupResources = rememberPlaygroundStartupResources()
        val systemDarkTheme = isSystemInDarkTheme()
        var darkTheme by remember { mutableStateOf(loadDarkThemePreference() ?: systemDarkTheme) }

        AppTheme(
            theme = docsTheme,
            darkTheme = darkTheme,
            useDynamicColors = false,
        ) {
            StartupGate(startupResources) {
                EditorRoute(
                    viewModel = viewModel,
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        darkTheme = !darkTheme
                        saveDarkThemePreference(darkTheme)
                    },
                )
            }
        }
    }
}

// Only text the editor shows on the first screen. Dialogs, menus, and content descriptions load when first used.
@Composable
private fun rememberPlaygroundStartupResources(): StartupResources {
    val iconResources =
        remember {
            ChartType.entries
                .map(::chartTypeIconResource)
                .distinct()
        }
    val stringResources =
        remember {
            listOf(
                Res.string.playground_title,
                Res.string.playground_editor_add_row,
                Res.string.playground_editor_randomize,
                Res.string.playground_editor_reset,
                Res.string.playground_editor_row_number_header,
                Res.string.playground_editor_preview_unchanged,
                Res.string.playground_chart_title_label,
                Res.string.playground_right_panel_settings,
                Res.string.playground_right_panel_code,
                Res.string.playground_code_title,
                Res.string.playground_code_copy,
                Res.string.playground_style_color_default,
                Res.string.playground_style_color_reset,
                Res.string.playground_style_palette_custom,
                Res.string.playground_style_palette_hint,
                Res.string.playground_style_palette_item_label,
            )
        }
    return remember(iconResources, stringResources) {
        StartupResources(
            vectorDrawables = iconResources + SharedRes.drawable.charts_logo,
            strings = stringResources,
        )
    }
}
