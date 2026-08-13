package presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_chart_selector_more
import chartsproject.playground.generated.resources.playground_chart_title_label
import chartsproject.playground.generated.resources.playground_code_copied
import chartsproject.playground.generated.resources.playground_code_copy
import chartsproject.playground.generated.resources.playground_code_copy_failed
import chartsproject.playground.generated.resources.playground_code_full
import chartsproject.playground.generated.resources.playground_code_minimal
import chartsproject.playground.generated.resources.playground_code_minimal_compact
import chartsproject.playground.generated.resources.playground_code_title
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
import chartsproject.playground.generated.resources.playground_editor_select_chart_content_description
import chartsproject.playground.generated.resources.playground_logo_content_description
import chartsproject.playground.generated.resources.playground_metadata_charts
import chartsproject.playground.generated.resources.playground_metadata_playground
import chartsproject.playground.generated.resources.playground_metadata_published
import chartsproject.playground.generated.resources.playground_right_panel_code
import chartsproject.playground.generated.resources.playground_right_panel_settings
import chartsproject.playground.generated.resources.playground_style_color_content_description
import chartsproject.playground.generated.resources.playground_style_override_default
import chartsproject.playground.generated.resources.playground_style_preset_palette_content_description
import chartsproject.playground.generated.resources.playground_style_use_preset_palette
import chartsproject.playground.generated.resources.playground_style_using_chart_defaults
import chartsproject.playground.generated.resources.playground_title
import chartsproject.sample_shared.generated.resources.charts_logo
import data.InMemoryEditorStore
import dev.hdcode.charts.sampleshared.startup.ChartsStartupGate
import dev.hdcode.charts.sampleshared.startup.StartupResources
import dev.hdcode.charts.sampleshared.startup.rememberStartupResourcesReady
import dev.hdcode.charts.sampleshared.theme.AppTheme
import dev.hdcode.charts.sampleshared.theme.docsSlate
import domain.ChartType
import platform.snapshotPublishMetadata
import presentation.editor.EditorRoute
import presentation.editor.EditorViewModel
import presentation.resources.chartTypeIconResource
import chartsproject.sample_shared.generated.resources.Res as SharedRes

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("Playground") {
        val viewModel =
            remember {
                EditorViewModel(
                    InMemoryEditorStore(snapshotMetadata = snapshotPublishMetadata()),
                )
            }

        val resourcesReady = rememberPlaygroundStartupResourcesReady()

        AppTheme(
            theme = docsSlate,
            useDynamicColors = false,
        ) {
            ChartsStartupGate(resourcesReady) {
                EditorRoute(viewModel)
            }
        }
    }
}

@Composable
private fun rememberPlaygroundStartupResourcesReady(): Boolean {
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
                Res.string.playground_logo_content_description,
                Res.string.playground_editor_add_row,
                Res.string.playground_editor_randomize,
                Res.string.playground_editor_reset,
                Res.string.playground_editor_row_number_header,
                Res.string.playground_editor_delete_row_content_description,
                Res.string.playground_editor_select_chart_content_description,
                Res.string.playground_editor_preview_unchanged,
                Res.string.playground_editor_reset_dialog_title,
                Res.string.playground_editor_randomize_dialog_title,
                Res.string.playground_editor_reset_dialog_message,
                Res.string.playground_editor_randomize_dialog_message,
                Res.string.playground_editor_cancel,
                Res.string.playground_metadata_charts,
                Res.string.playground_metadata_playground,
                Res.string.playground_metadata_published,
                Res.string.playground_chart_title_label,
                Res.string.playground_chart_selector_more,
                Res.string.playground_right_panel_settings,
                Res.string.playground_right_panel_code,
                Res.string.playground_code_title,
                Res.string.playground_code_minimal,
                Res.string.playground_code_minimal_compact,
                Res.string.playground_code_full,
                Res.string.playground_code_copy,
                Res.string.playground_code_copied,
                Res.string.playground_code_copy_failed,
                Res.string.playground_style_override_default,
                Res.string.playground_style_use_preset_palette,
                Res.string.playground_style_using_chart_defaults,
                Res.string.playground_style_color_content_description,
                Res.string.playground_style_preset_palette_content_description,
            )
        }
    val resources =
        remember(iconResources, stringResources) {
            StartupResources(
                bitmapDrawables = listOf(SharedRes.drawable.charts_logo),
                vectorDrawables = iconResources,
                strings = stringResources,
            )
        }

    return rememberStartupResourcesReady(resources)
}
