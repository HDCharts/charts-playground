package codegen.bar

import codegen.BarCodegenConfig
import codegen.PieSliceInput
import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.ColorValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarChartCodeGeneratorTest {
    private val generator = BarChartCodeGenerator()

    @Test
    fun default_points_generate_snippet_without_style() {
        val snippet =
            generator.generate(
                BarCodegenConfig(
                    points =
                        listOf(
                            PieSliceInput(label = "Mon", value = 12f),
                            PieSliceInput(label = "Tue", value = 18f),
                        ),
                ),
            )

        assertTrue(snippet.code.contains("import io.github.dautovicharis.charts.BarChart"))
        assertTrue(snippet.code.contains("fun PlaygroundBarChartExample()"))
        assertTrue(snippet.code.contains("BarChart(dataSet = dataSet)"))
        assertFalse(snippet.code.contains("BarChartDefaults.style("))
    }

    @Test
    fun omits_defaults_and_keeps_changes() {
        val snippet =
            generator.generate(
                BarCodegenConfig(
                    points =
                        listOf(
                            PieSliceInput(label = "A", value = 1f),
                            PieSliceInput(label = "B", value = 2f),
                        ),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current = listOf(styleProperty("barAlpha", 0.8f), styleProperty("gridVisible", false)),
                            defaults = listOf(styleProperty("barAlpha", 0.8f), styleProperty("gridVisible", true)),
                        ),
                ),
            )

        assertFalse(snippet.code.contains("barAlpha = 0.8f,"))
        assertTrue(snippet.code.contains("gridVisible = false,"))
    }

    @Test
    fun style_with_bar_colors_emits_palette_and_color_import() {
        val snippet =
            generator.generate(
                BarCodegenConfig(
                    points =
                        listOf(
                            PieSliceInput(label = "A", value = 1f),
                            PieSliceInput(label = "B", value = 2f),
                        ),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current =
                                listOf(
                                    styleProperty(
                                        "barColors",
                                        listOf(ColorValue(0xFFFF0000L), ColorValue(0xFF00FF00L)),
                                    ),
                                ),
                            defaults = listOf(styleProperty("barColors", emptyList())),
                        ),
                ),
            )

        assertTrue(snippet.code.contains("import androidx.compose.ui.graphics.Color"))
        assertTrue(snippet.code.contains("barColors = listOf(Color("))
    }
}
