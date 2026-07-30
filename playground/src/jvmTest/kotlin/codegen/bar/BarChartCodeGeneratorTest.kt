package codegen.bar

import codegen.BarCodegenConfig
import codegen.PieSliceInput
import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.CodegenMode
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
                            PieSliceInput(label = "Mon", valueText = "12"),
                            PieSliceInput(label = "Tue", valueText = "18"),
                        ),
                ),
            )

        assertTrue(snippet.code.contains("import io.github.dautovicharis.charts.BarChart"))
        assertTrue(snippet.code.contains("fun PlaygroundBarChartExample()"))
        assertTrue(snippet.code.contains("BarChart(dataSet = dataSet)"))
        assertFalse(snippet.code.contains("BarChartDefaults.style("))
    }

    @Test
    fun minimal_mode_omits_defaults_and_keeps_changes() {
        val snippet =
            generator.generate(
                BarCodegenConfig(
                    points =
                        listOf(
                            PieSliceInput(label = "A", valueText = "1"),
                            PieSliceInput(label = "B", valueText = "2"),
                        ),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current = listOf(styleProperty("barAlpha", 0.8f), styleProperty("gridVisible", false)),
                            defaults = listOf(styleProperty("barAlpha", 0.8f), styleProperty("gridVisible", true)),
                        ),
                    codegenMode = CodegenMode.MINIMAL,
                ),
            )

        assertFalse(snippet.code.contains("barAlpha = 0.8f,"))
        assertTrue(snippet.code.contains("gridVisible = false,"))
    }

    @Test
    fun full_mode_emits_values_that_match_defaults() {
        val snippet =
            generator.generate(
                BarCodegenConfig(
                    points =
                        listOf(
                            PieSliceInput(label = "A", valueText = "1"),
                            PieSliceInput(label = "B", valueText = "2"),
                        ),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current =
                                listOf(
                                    styleProperty("axisVisible", true),
                                    styleProperty("selectionLineWidth", 1.5f),
                                ),
                            defaults =
                                listOf(
                                    styleProperty("axisVisible", true),
                                    styleProperty("selectionLineWidth", 1.5f),
                                ),
                        ),
                    codegenMode = CodegenMode.FULL,
                ),
            )

        assertTrue(snippet.code.contains("axisVisible = true,"))
        assertTrue(snippet.code.contains("selectionLineWidth = 1.5f,"))
    }

    @Test
    fun style_with_bar_colors_emits_palette_and_color_import() {
        val snippet =
            generator.generate(
                BarCodegenConfig(
                    points =
                        listOf(
                            PieSliceInput(label = "A", valueText = "1"),
                            PieSliceInput(label = "B", valueText = "2"),
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
                    codegenMode = CodegenMode.MINIMAL,
                ),
            )

        assertTrue(snippet.code.contains("import androidx.compose.ui.graphics.Color"))
        assertTrue(snippet.code.contains("barColors = listOf(Color("))
    }
}
