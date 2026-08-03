package codegen.area

import codegen.AreaCodegenConfig
import codegen.MultiSeriesCodegenInput
import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.CodegenMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AreaChartCodeGeneratorTest {
    private val generator = AreaChartCodeGenerator()

    @Test
    fun default_series_generate_snippet_without_style() {
        val snippet =
            generator.generate(
                AreaCodegenConfig(
                    series =
                        listOf(
                            MultiSeriesCodegenInput("Plan", listOf(60f, 80f)),
                            MultiSeriesCodegenInput("Actual", listOf(55f, 70f)),
                        ),
                    categories = listOf("Jan", "Feb"),
                ),
            )

        assertTrue(snippet.code.contains("import io.github.dautovicharis.charts.StackedAreaChart"))
        assertTrue(snippet.code.contains("fun PlaygroundAreaChartExample()"))
        assertTrue(snippet.code.contains("StackedAreaChart(dataSet = dataSet)"))
        assertFalse(snippet.code.contains("StackedAreaChartDefaults.style("))
    }

    @Test
    fun minimal_mode_omits_defaults_and_keeps_changes() {
        val snippet =
            generator.generate(
                AreaCodegenConfig(
                    series = listOf(MultiSeriesCodegenInput("Free", listOf(60f, 80f))),
                    categories = listOf("Jan", "Feb"),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current = listOf(styleProperty("fillAlpha", 0.4f), styleProperty("lineVisible", false)),
                            defaults = listOf(styleProperty("fillAlpha", 0.4f), styleProperty("lineVisible", true)),
                        ),
                    codegenMode = CodegenMode.MINIMAL,
                ),
            )

        assertFalse(snippet.code.contains("fillAlpha = 0.4f,"))
        assertTrue(snippet.code.contains("lineVisible = false,"))
    }

    @Test
    fun full_mode_emits_values_that_match_defaults() {
        val snippet =
            generator.generate(
                AreaCodegenConfig(
                    series = listOf(MultiSeriesCodegenInput("Free", listOf(60f, 80f))),
                    categories = listOf("Jan", "Feb"),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current = listOf(styleProperty("fillAlpha", 0.4f), styleProperty("bezier", true)),
                            defaults = listOf(styleProperty("fillAlpha", 0.4f), styleProperty("bezier", true)),
                        ),
                    codegenMode = CodegenMode.FULL,
                ),
            )

        assertTrue(snippet.code.contains("fillAlpha = 0.4f,"))
        assertTrue(snippet.code.contains("bezier = true,"))
    }
}
