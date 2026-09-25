package data.charts

import data.PlaygroundChart
import domain.ChartStyleState
import domain.ColorValue
import domain.PIE_SLICE_COLORS_PATH
import domain.StyleValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Generated style code, derived from each setting's path and kind. */
class StyleCodegenTest {
    @Test
    fun unset_values_generate_no_style() {
        val code = LineChartDefinition.generateWith()

        assertFalse(code.contains("LineChartDefaults.style("))
    }

    @Test
    fun a_setting_becomes_an_argument_of_its_style_block() {
        val code = LineChartDefinition.generateWith("line.alpha" to StyleValue.Number(0.8f))

        assertTrue(code.contains("line = LineChartDefaults.line(alpha = 0.8f,),"), code)
    }

    @Test
    fun values_are_written_in_their_library_types() {
        val code =
            LineChartDefinition.generateWith(
                "points.size" to StyleValue.Number(12f),
                "axis.color" to StyleValue.Color(ColorValue(0xFF112233L)),
                "zoomControlsVisible" to StyleValue.Bool(false),
            )

        assertTrue(code.contains("points = LineChartDefaults.points(size = 12.dp,),"), code)
        assertTrue(code.contains("import androidx.compose.ui.unit.dp"), code)
        assertTrue(code.contains("axis = LineChartDefaults.axis(color = Color(0xFF112233),),"), code)
        assertTrue(code.contains("zoomControlsVisible = false,"), code)
    }

    @Test
    fun nested_label_settings_share_one_factory_call() {
        val code =
            BarChartDefinition.generateWith(
                "axis.xLabels.visible" to StyleValue.Bool(false),
                "axis.xLabels.count" to StyleValue.Number(4f),
            )

        assertTrue(
            code.contains(
                "axis = BarChartDefaults.axis(xLabels = BarChartDefaults.xLabels(visible = false, count = 4),),",
            ),
            code,
        )
    }

    @Test
    fun histogram_builds_shared_blocks_with_bar_defaults() {
        val code =
            HistogramChartDefinition.generateWith(
                "bars.alpha" to StyleValue.Number(0.5f),
                "grid.visible" to StyleValue.Bool(false),
            )

        assertTrue(code.contains("bars = HistogramChartDefaults.bars(alpha = 0.5f,),"), code)
        assertTrue(code.contains("grid = BarChartDefaults.grid(visible = false,),"), code)
        assertTrue(code.contains("import io.github.hdcharts.charts.style.BarChartDefaults"), code)
    }

    @Test
    fun fixed_range_is_only_written_while_switched_on() {
        val off = LineChartDefinition.generateWith("range.min" to StyleValue.Number(10f))
        val on =
            LineChartDefinition.generateWith(
                "range.fixed" to StyleValue.Bool(true),
                "range.min" to StyleValue.Number(10f),
            )

        assertFalse(off.contains("range ="), off)
        assertFalse(on.contains("fixed"), on)
        assertTrue(on.contains("range = LineChartDefaults.range(min = 10.0,),"), on)
    }

    @Test
    fun histogram_range_keeps_its_zero_min_when_only_max_is_set() {
        val maxOnly =
            HistogramChartDefinition.generateWith(
                "range.fixed" to StyleValue.Bool(true),
                "range.max" to StyleValue.Number(50f),
            )
        val both =
            HistogramChartDefinition.generateWith(
                "range.fixed" to StyleValue.Bool(true),
                "range.min" to StyleValue.Number(5f),
                "range.max" to StyleValue.Number(50f),
            )

        assertTrue(maxOnly.contains("range = BarChartDefaults.range(min = 0.0, max = 50.0,),"), maxOnly)
        assertTrue(both.contains("range = BarChartDefaults.range(min = 5.0, max = 50.0,),"), both)
    }

    @Test
    fun pie_slice_colors_go_on_the_data_rows() {
        val code =
            PieChartDefinition.generateWith(
                PIE_SLICE_COLORS_PATH to StyleValue.Colors(listOf(ColorValue(0xFFFF0000L))),
            )

        assertTrue(code.contains("color = Color(0xFFFF0000)),"), code)
        assertFalse(code.contains("PieChartDefaults.style("), code)
    }
}

private fun PlaygroundChart.generateWith(vararg values: Pair<String, StyleValue>): String {
    val spec = resetSession().validatedSpec.copy(styleState = ChartStyleState(values.toMap()))
    return generate(spec)
}
