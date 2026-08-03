package codegen.line

import codegen.ChartCodeGenerator
import codegen.GeneratedSnippet
import codegen.LineCodegenConfig
import codegen.LinePointInput
import codegen.common.ChartCodeRenderer
import codegen.common.NormalizedPoint
import codegen.common.buildChartImports
import codegen.common.resolveStyleArguments

internal class LineChartCodeGenerator(
    private val renderer: ChartCodeRenderer = ChartCodeRenderer(),
) : ChartCodeGenerator<LineCodegenConfig> {
    override fun generate(config: LineCodegenConfig): GeneratedSnippet {
        val items = normalizePoints(config.points)
        val styleArguments = resolveStyleArguments(config.styleProperties, config.codegenMode)
        val includeStyle = styleArguments.isNotEmpty()
        val imports =
            buildChartImports(
                baseImports = BASE_IMPORTS,
                styleImport = if (includeStyle) STYLE_IMPORT else null,
                styleArguments = styleArguments,
            )
        val bodyLines = mutableListOf<String>()

        bodyLines += renderer.renderDataSet(items, config.title)
        bodyLines += ""

        if (includeStyle) {
            bodyLines += renderer.renderStyle(STYLE_BUILDER, styleArguments)
            bodyLines += ""
        }

        bodyLines += renderer.renderChartCall(COMPONENT_NAME, includeStyle)

        val code = renderer.renderFunction(imports, config.functionName, bodyLines)
        return GeneratedSnippet(code = code)
    }

    private fun normalizePoints(points: List<LinePointInput>): List<NormalizedPoint> =
        points.mapIndexed { index, point ->
            val sanitizedLabel = point.label.trim().ifBlank { "Point ${index + 1}" }
            NormalizedPoint(
                label = sanitizedLabel,
                value = point.value,
            )
        }

    private companion object {
        val BASE_IMPORTS =
            listOf(
                "import androidx.compose.runtime.Composable",
                "import io.github.dautovicharis.charts.LineChart",
                "import io.github.dautovicharis.charts.model.toChartDataSet",
            )
        const val STYLE_IMPORT = "import io.github.dautovicharis.charts.style.LineChartDefaults"
        const val COMPONENT_NAME = "LineChart"
        const val STYLE_BUILDER = "LineChartDefaults.style"
    }
}
