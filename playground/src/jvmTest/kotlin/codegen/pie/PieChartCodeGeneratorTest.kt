package codegen.pie

import codegen.PieCodegenConfig
import codegen.PieSliceInput
import codegen.StylePropertiesSnapshot
import codegen.styleProperty
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
        assertTrue(snippet.code.contains("import androidx.compose.runtime.remember"))
        assertTrue(snippet.code.contains("import io.github.dautovicharis.charts.PieChart"))
        assertTrue(snippet.code.contains("fun PlaygroundPieChartExample()"))
        assertTrue(snippet.code.contains("val data ="))
        assertTrue(snippet.code.contains("    remember {"))
        assertTrue(snippet.code.contains("PieChart(data = data, title = \"Revenue Breakdown\")"))
        assertFalse(snippet.code.contains("PieChartDefaults.style("))
    }

    @Test
    fun emits_only_non_default_arguments() {
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
                ),
            )

        assertTrue(snippet.code.contains("legend = PieChartDefaults.legend(visible = false,)"))
        assertFalse(snippet.code.contains("border = PieChartDefaults.border("))
    }

    @Test
    fun emits_slice_colors_on_the_data_rows_not_as_style() {
        val snippet =
            generator.generate(
                PieCodegenConfig(
                    rows =
                        listOf(
                            PieSliceInput(label = "A", value = 1f, color = ColorValue(0xFF1D3557L)),
                            PieSliceInput(label = "B", value = 2f, color = ColorValue(0xFF457B9DL)),
                        ),
                ),
            )

        assertTrue(snippet.code.contains("import androidx.compose.ui.graphics.Color"))
        assertTrue(snippet.code.contains("listOf("))
        assertTrue(
            snippet.code.contains(
                "            PieSlice(label = \"A\", value = 1f, color = Color(0xFF1D3557)),",
            ),
        )
        assertTrue(
            snippet.code.contains(
                "            PieSlice(label = \"B\", value = 2f, color = Color(0xFF457B9D)),",
            ),
        )
        assertFalse(snippet.code.contains("PieChartDefaults.slices(colors"))
    }
}
