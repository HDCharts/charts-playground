package codegen.radar

import codegen.MultiSeriesCodegenInput
import codegen.RadarCodegenConfig
import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RadarChartCodeGeneratorTest {
    private val generator = RadarChartCodeGenerator()

    @Test
    fun default_series_generate_snippet_without_style() {
        val snippet =
            generator.generate(
                RadarCodegenConfig(
                    series =
                        listOf(
                            MultiSeriesCodegenInput("Android", listOf(80f, 75f, 70f)),
                            MultiSeriesCodegenInput("iOS", listOf(78f, 74f, 72f)),
                        ),
                    categories = listOf("Perf", "UX", "Security"),
                ),
            )

        assertTrue(snippet.code.contains("import io.github.dautovicharis.charts.RadarChart"))
        assertTrue(snippet.code.contains("fun PlaygroundRadarChartExample()"))
        assertTrue(snippet.code.contains("RadarChart(dataSet = dataSet)"))
        assertFalse(snippet.code.contains("RadarChartDefaults.style("))
    }

    @Test
    fun omits_defaults_and_keeps_changes() {
        val snippet =
            generator.generate(
                RadarCodegenConfig(
                    series = listOf(MultiSeriesCodegenInput("Android", listOf(80f, 75f, 70f))),
                    categories = listOf("Perf", "UX", "Security"),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current = listOf(styleProperty("lineWidth", 2f), styleProperty("gridVisible", false)),
                            defaults = listOf(styleProperty("lineWidth", 2f), styleProperty("gridVisible", true)),
                        ),
                ),
            )

        assertFalse(snippet.code.contains("lineWidth = 2f,"))
        assertTrue(snippet.code.contains("gridVisible = false,"))
    }
}
