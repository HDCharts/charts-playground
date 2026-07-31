package codegen.pie

import codegen.PieCodegenConfig
import codegen.PieSliceInput
import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.CodegenMode
import domain.ColorValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PieChartCodeGeneratorTest {
    private val generator = PieChartCodeGenerator()

    @Test
    fun default_rows_generate_snippet_without_style_block() {
        val snippet =
            generator.generate(
                PieCodegenConfig(
                    rows =
                        listOf(
                            PieSliceInput(label = "Product A", value = 24f),
                            PieSliceInput(label = "Product B", value = 18f),
                        ),
                ),
            )

        assertTrue(snippet.code.contains("import androidx.compose.runtime.Composable"))
        assertTrue(snippet.code.contains("import io.github.dautovicharis.charts.PieChart"))
        assertTrue(snippet.code.contains("fun PlaygroundPieChartExample()"))
        assertTrue(snippet.code.contains("PieChart(dataSet = dataSet)"))
        assertFalse(snippet.code.contains("PieChartDefaults.style("))
    }

    @Test
    fun minimal_mode_emits_only_non_default_arguments() {
        val snippet =
            generator.generate(
                PieCodegenConfig(
                    rows =
                        listOf(
                            PieSliceInput(label = "A", value = 1f),
                            PieSliceInput(label = "B", value = 2f),
                        ),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current = listOf(styleProperty("borderWidth", 3f), styleProperty("legendVisible", false)),
                            defaults = listOf(styleProperty("borderWidth", 3f), styleProperty("legendVisible", true)),
                        ),
                    codegenMode = CodegenMode.MINIMAL,
                ),
            )

        assertTrue(snippet.code.contains("legendVisible = false,"))
        assertFalse(snippet.code.contains("borderWidth = 3f,"))
    }

    @Test
    fun full_mode_emits_all_current_arguments_even_if_default() {
        val snippet =
            generator.generate(
                PieCodegenConfig(
                    rows =
                        listOf(
                            PieSliceInput(label = "A", value = 1f),
                            PieSliceInput(label = "B", value = 2f),
                            PieSliceInput(label = "C", value = 3f),
                        ),
                    styleProperties =
                        StylePropertiesSnapshot(
                            current =
                                listOf(
                                    styleProperty("borderWidth", 3f),
                                    styleProperty(
                                        "pieColors",
                                        listOf(
                                            ColorValue(0xFF1D3557L),
                                            ColorValue(0xFF457B9DL),
                                            ColorValue(0xFFA8DADCL),
                                        ),
                                    ),
                                ),
                            defaults =
                                listOf(
                                    styleProperty("borderWidth", 3f),
                                    styleProperty(
                                        "pieColors",
                                        listOf(
                                            ColorValue(0xFF1D3557L),
                                            ColorValue(0xFF457B9DL),
                                            ColorValue(0xFFA8DADCL),
                                        ),
                                    ),
                                ),
                        ),
                    codegenMode = CodegenMode.FULL,
                ),
            )

        assertTrue(snippet.code.contains("borderWidth = 3f,"))
        assertTrue(
            snippet.code.contains("pieColors = listOf(Color(0xFF1D3557), Color(0xFF457B9D), Color(0xFFA8DADC)),"),
        )
        assertTrue(snippet.code.contains("import androidx.compose.ui.graphics.Color"))
    }
}
