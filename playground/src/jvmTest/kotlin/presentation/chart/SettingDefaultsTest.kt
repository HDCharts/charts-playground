package presentation.chart

import data.chartCatalog
import data.charts.BarChartDefinition
import data.charts.HistogramChartDefinition
import domain.ChartData
import domain.ChartStyleState
import domain.ColorValue
import domain.StyleKind
import domain.StyleValue
import domain.styleSettings
import testing.readComposable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every setting must show a default of its kind while the user has not set it. Resolving fails
 * outright when a default is missing or of a library value type `toStyleValue` does not know.
 */
class SettingDefaultsTest {
    @Test
    fun every_setting_has_a_default_of_its_kind() {
        val problems =
            chartCatalog.charts.flatMap { definition ->
                val data = definition.resetSession().validatedSpec.data
                val resolver =
                    readComposable {
                        rememberSettingValues(definition.type, definition.settings, ChartStyleState(), data)
                    }
                definition.settings.styleSettings.mapNotNull { setting ->
                    val value = resolver.value(setting.path) ?: return@mapNotNull null
                    "${definition.type} '${setting.path}': default $value is not a ${setting.kind}"
                        .takeUnless { setting.kind.accepts(value) }
                }
            }

        assertTrue(problems.isEmpty(), problems.joinToString(separator = "\n"))
    }

    @Test
    fun per_bar_colors_default_to_the_chosen_bar_color() {
        val red = ColorValue(0xFFFF0000)
        listOf(BarChartDefinition, HistogramChartDefinition).forEach { definition ->
            val data = definition.resetSession().validatedSpec.data as ChartData.SingleSeries
            val style = ChartStyleState().with("bars.color", StyleValue.Color(red))
            val resolver =
                readComposable { rememberSettingValues(definition.type, definition.settings, style, data) }

            assertEquals(
                StyleValue.Colors(List(data.values.size) { red }),
                resolver.value("bars.colors"),
                "${definition.type}",
            )
        }
    }
}

private fun StyleKind.accepts(value: StyleValue): Boolean =
    when (this) {
        StyleKind.BOOLEAN -> value is StyleValue.Bool
        StyleKind.FLOAT, StyleKind.DP, StyleKind.INT, StyleKind.DOUBLE -> value is StyleValue.Number
        StyleKind.COLOR -> value is StyleValue.Color
        StyleKind.COLOR_LIST -> value is StyleValue.Colors
    }
