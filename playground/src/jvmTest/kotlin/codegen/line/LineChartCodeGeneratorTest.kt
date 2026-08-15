package codegen.line

import codegen.LineCodegenConfig
import codegen.LinePointInput
import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LineChartCodeGeneratorTest {
    private val generator = LineChartCodeGenerator()

    @Test
    fun default_points_generate_snippet_without_style() {
        val snippet =
            generator.generate(
                LineCodegenConfig(
                    points =
                        listOf(
                            LinePointInput(label = "Jan", value = 12f),
                            LinePointInput(label = "Feb", value = 18f),
                        ),
                ),
            )

        assertTrue(snippet.code.contains("import io.github.dautovicharis.charts.LineChart"))
        assertTrue(snippet.code.contains("fun PlaygroundLineChartExample()"))
        assertTrue(snippet.code.contains("LineChart(dataSet = dataSet)"))
        assertFalse(snippet.code.contains("LineChartDefaults.style("))
    }

    @Test
    fun omits_defaults_and_keeps_changes() {
        val snippet =
            generator.generate(
                LineCodegenConfig(
                    points =
                        listOf(
                            LinePointInput(label = "A", value = 1f),
                            LinePointInput(label = "B", value = 2f),
                        ),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current = listOf(styleProperty("bezier", true), styleProperty("axisVisible", false)),
                            defaults = listOf(styleProperty("bezier", true), styleProperty("axisVisible", true)),
                        ),
                ),
            )

        assertFalse(snippet.code.contains("bezier = true,"))
        assertTrue(snippet.code.contains("axisVisible = false,"))
    }

    @Test
    fun generator_uses_custom_function_name_when_provided() {
        val snippet =
            generator.generate(
                LineCodegenConfig(
                    points =
                        listOf(
                            LinePointInput(label = "A", value = 1f),
                            LinePointInput(label = "B", value = 2f),
                        ),
                    functionName = "MonthlyTrendLineChart",
                ),
            )

        assertTrue(snippet.code.contains("fun MonthlyTrendLineChart()"))
    }
}
