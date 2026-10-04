package codegen

import codegen.common.RenderedStyleArgument
import testing.assertSnippetCompiles
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChartSnippetTest {
    @Test
    fun values_render_a_chart_call_without_style() {
        val code = snippet("BarChart", values).render()

        assertTrue(code.contains("import io.github.hdcharts.bar.BarChart"), code)
        assertTrue(code.contains("import io.github.hdcharts.core.model.toChartData"), code)
        assertTrue(code.contains("fun ExampleChart()"), code)
        assertTrue(code.contains("listOf(12.0, 18.0).toChartData("), code)
        assertTrue(code.contains("categories = listOf(\"Mon\", \"Tue\"),"), code)
        assertTrue(code.contains("BarChart(data = data, title = \"Weekly\")"), code)
        assertFalse(code.contains("BarChartDefaults"), code)
    }

    @Test
    fun style_arguments_build_the_style_with_the_defaults_object() {
        val code =
            snippet("BarChart", values)
                .copy(styleArguments = listOf(RenderedStyleArgument("zoomControlsVisible = false,")))
                .render()

        assertTrue(code.contains("import io.github.hdcharts.core.style.BarChartDefaults"), code)
        assertTrue(code.contains("        BarChartDefaults.style(\n            zoomControlsVisible = false,\n"), code)
        assertTrue(code.contains("        style = style,"), code)
    }

    @Test
    fun series_and_title_escape_dollar_signs() {
        val template = "$" + "{value}"
        val code =
            snippet("LineChart", SnippetData.Series(listOf("Revenue $" to listOf(120f, 140f)), listOf("Q$1", template)))
                .copy(title = "Growth $")
                .render()

        assertTrue(code.contains("\"Revenue \\$\" to listOf(120.0, 140.0),"), code)
        assertTrue(code.contains("categories = listOf(\"Q\\$1\", \"\\$" + "{value}\"),"), code)
        assertTrue(code.contains("LineChart(data = data, title = \"Growth \\$\")"), code)
    }

    @Test
    fun pie_writes_the_shared_data_shape() {
        val code = snippet("PieChart", values).render()

        assertTrue(code.contains("import io.github.hdcharts.core.model.toChartData"), code)
        assertTrue(code.contains("listOf(12.0, 18.0).toChartData("), code)
        assertTrue(code.contains("categories = listOf(\"Mon\", \"Tue\"),"), code)
    }

    @Test
    fun empty_data_still_renders_complete_source() {
        val single = snippet("LineChart", SnippetData.Values(emptyList(), emptyList())).render()
        val multi = snippet("LineChart", SnippetData.Series(emptyList(), emptyList())).render()

        assertTrue("listOf().toChartData(" in single, single)
        assertTrue("val items = listOf(" in multi, multi)
        assertTrue(single.endsWith("}\n") && multi.endsWith("}\n"))
    }

    @Test
    fun every_data_shape_compiles() {
        listOf(
            snippet("BarChart", values),
            snippet("RadarChart", SnippetData.Series(listOf("Android" to listOf(80f, 75f)), listOf("Perf", "UX"))),
            snippet("PieChart", values),
        ).forEach { snippet -> assertSnippetCompiles(snippet.render(), snippet.component) }
    }
}

private val values = SnippetData.Values(values = listOf(12f, 18f), categories = listOf("Mon", "Tue"))

private fun snippet(
    component: String,
    data: SnippetData,
): ChartSnippet =
    ChartSnippet(
        component = component,
        styleObject = component.replace("Chart", "ChartDefaults"),
        data = data,
        title = "Weekly",
        functionName = "ExampleChart",
    )
