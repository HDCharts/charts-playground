package codegen.histogram

import codegen.BarPointInput
import codegen.ChartCodeGenerator
import codegen.GeneratedSnippet
import codegen.HistogramCodegenConfig
import codegen.common.ChartCodeRenderer
import codegen.common.NormalizedPoint
import codegen.common.buildChartImports
import codegen.common.resolveStyleArguments

internal class HistogramChartCodeGenerator(
    private val renderer: ChartCodeRenderer = ChartCodeRenderer(),
) : ChartCodeGenerator<HistogramCodegenConfig> {
    override fun generate(config: HistogramCodegenConfig): GeneratedSnippet {
        val items = normalizePoints(config.points)
        val styleArguments = resolveStyleArguments(config.styleProperties, STYLE_BUILDER)
        val includeStyle = styleArguments.isNotEmpty()
        val imports =
            buildChartImports(
                baseImports = BASE_IMPORTS,
                styleImport = if (includeStyle) STYLE_IMPORT else null,
                styleArguments = styleArguments,
            )
        val bodyLines = mutableListOf<String>()
        bodyLines += renderer.renderData(items)
        bodyLines += ""

        if (includeStyle) {
            bodyLines += renderer.renderStyle(STYLE_BUILDER, styleArguments)
            bodyLines += ""
        }

        bodyLines += renderer.renderChartCall(COMPONENT_NAME, config.title, includeStyle)
        val code = renderer.renderFunction(imports, config.functionName, bodyLines)
        return GeneratedSnippet(code = code)
    }

    private fun normalizePoints(points: List<BarPointInput>): List<NormalizedPoint> =
        points.mapIndexed { index, point ->
            val sanitizedLabel = point.label.trim().ifBlank { "Bin ${index + 1}" }
            NormalizedPoint(
                label = sanitizedLabel,
                value = point.value,
            )
        }

    private companion object {
        val BASE_IMPORTS =
            listOf(
                "import androidx.compose.runtime.Composable",
                "import io.github.hdcharts.charts.HistogramChart",
                "import io.github.hdcharts.charts.model.toChartData",
            )
        const val STYLE_IMPORT = "import io.github.hdcharts.charts.style.HistogramChartDefaults"
        const val COMPONENT_NAME = "HistogramChart"
        const val STYLE_BUILDER = "HistogramChartDefaults.style"
    }
}
