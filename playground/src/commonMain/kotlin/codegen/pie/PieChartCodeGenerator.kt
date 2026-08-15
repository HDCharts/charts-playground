package codegen.pie

import codegen.ChartCodeGenerator
import codegen.GeneratedSnippet
import codegen.PieCodegenConfig
import codegen.PieSliceInput
import codegen.common.ChartCodeRenderer
import codegen.common.NormalizedPoint
import codegen.common.buildChartImports
import codegen.common.colorLiteral
import codegen.common.escapeKotlinString
import codegen.common.formatKotlinFloatLiteral
import codegen.common.kotlinLine

internal class PieChartCodeGenerator(
    private val renderer: ChartCodeRenderer = ChartCodeRenderer(),
) : ChartCodeGenerator<PieCodegenConfig> {
    override fun generate(config: PieCodegenConfig): GeneratedSnippet {
        val items = normalizeRows(config.rows)
        val styleArguments = resolvePieStyleArguments(config.styleProperties)
        val includeStyle = styleArguments.isNotEmpty()
        val colorImport = if (items.any { it.color != null }) COLOR_IMPORT else null
        val imports =
            buildChartImports(
                baseImports = BASE_IMPORTS,
                styleImport = if (includeStyle) STYLE_IMPORT else null,
                styleArguments = styleArguments,
                extraImports = colorImport?.let { listOf(it) } ?: emptyList(),
            )
        val bodyLines = mutableListOf<String>()

        bodyLines += renderData(items)
        bodyLines += ""

        if (includeStyle) {
            bodyLines += renderer.renderStyle(STYLE_BUILDER, styleArguments)
            bodyLines += ""
        }

        bodyLines += renderChartCall(config.title, includeStyle)

        val code = renderer.renderFunction(imports, config.functionName, bodyLines)
        return GeneratedSnippet(code = code)
    }

    private fun renderData(items: List<NormalizedPoint>): List<String> {
        val sliceBody =
            items.joinToString("\n") { item ->
                val args =
                    listOfNotNull(
                        "label = \"${escapeKotlinString(item.label)}\"",
                        "value = ${formatKotlinFloatLiteral(item.value)}",
                        item.color?.let { "color = ${colorLiteral(it)}" },
                    ).joinToString(", ")
                kotlinLine(3, "PieSlice($args),")
            }

        return buildList {
            add(kotlinLine(1, "val data ="))
            add(kotlinLine(1, "remember {"))
            add(kotlinLine(2, "listOf("))
            add(sliceBody)
            add(kotlinLine(2, ")"))
            add(kotlinLine(1, "}"))
        }
    }

    private fun renderChartCall(
        title: String,
        includeStyle: Boolean,
    ): List<String> =
        if (includeStyle) {
            listOf(
                kotlinLine(1, "PieChart("),
                kotlinLine(2, "data = data,"),
                kotlinLine(2, "title = \"${escapeKotlinString(title)}\","),
                kotlinLine(2, "style = style,"),
                kotlinLine(1, ")"),
            )
        } else {
            listOf(kotlinLine(1, "PieChart(data = data, title = \"${escapeKotlinString(title)}\")"))
        }

    private fun normalizeRows(rows: List<PieSliceInput>): List<NormalizedPoint> =
        rows.mapIndexed { index, row ->
            val sanitizedLabel = row.label.trim().ifBlank { "Slice ${index + 1}" }
            NormalizedPoint(
                label = sanitizedLabel,
                value = row.value,
                color = row.color,
            )
        }

    private companion object {
        const val COLOR_IMPORT = "import androidx.compose.ui.graphics.Color"
        val BASE_IMPORTS =
            listOf(
                "import androidx.compose.runtime.Composable",
                "import androidx.compose.runtime.remember",
                "import io.github.dautovicharis.charts.PieChart",
                "import io.github.dautovicharis.charts.model.PieSlice",
            )
        const val STYLE_IMPORT = "import io.github.dautovicharis.charts.style.PieChartDefaults"
        const val STYLE_BUILDER = "PieChartDefaults.style"
    }
}
