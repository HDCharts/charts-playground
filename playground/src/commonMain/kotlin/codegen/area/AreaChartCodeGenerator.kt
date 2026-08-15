package codegen.area

import codegen.AreaCodegenConfig
import codegen.ChartCodeGenerator
import codegen.GeneratedSnippet
import codegen.common.ChartCodeRenderer
import codegen.common.MultiSeriesItem
import codegen.common.buildChartImports
import codegen.common.buildMultiChartDataSetCode
import codegen.common.resolveStyleArguments
import kotlin.math.max

internal class AreaChartCodeGenerator(
    private val renderer: ChartCodeRenderer = ChartCodeRenderer(),
) : ChartCodeGenerator<AreaCodegenConfig> {
    override fun generate(config: AreaCodegenConfig): GeneratedSnippet {
        val normalized = normalizeSeries(config)
        val styleArguments = resolveStyleArguments(config.styleProperties)
        val includeStyle = styleArguments.isNotEmpty()
        val imports =
            buildChartImports(
                baseImports = BASE_IMPORTS,
                styleImport = if (includeStyle) STYLE_IMPORT else null,
                styleArguments = styleArguments,
            )

        val bodyLines = mutableListOf<String>()
        bodyLines +=
            buildMultiChartDataSetCode(
                items = normalized.series,
                title = config.title,
                categories = normalized.categories,
            )
        bodyLines += ""

        if (includeStyle) {
            bodyLines += renderer.renderStyle(STYLE_BUILDER, styleArguments)
            bodyLines += ""
        }

        bodyLines += renderer.renderChartCall(COMPONENT_NAME, includeStyle)
        val code = renderer.renderFunction(imports, config.functionName, bodyLines)
        return GeneratedSnippet(code = code)
    }

    private fun normalizeSeries(config: AreaCodegenConfig): NormalizedMultiSeries {
        val initialCategories =
            config.categories.mapIndexed { index, label ->
                label.trim().ifBlank { "Point ${index + 1}" }
            }
        val targetSize = max(initialCategories.size, config.series.maxOfOrNull { it.values.size } ?: 0)
        val categories =
            List(targetSize) { index ->
                initialCategories.getOrNull(index) ?: "Point ${index + 1}"
            }

        val series =
            config.series.mapIndexed { index, item ->
                val label = item.label.trim().ifBlank { "Series ${index + 1}" }
                val values =
                    List(targetSize) { valueIndex ->
                        item.values.getOrElse(valueIndex) { 0f }
                    }
                MultiSeriesItem(label = label, values = values)
            }

        return NormalizedMultiSeries(categories = categories, series = series)
    }

    private data class NormalizedMultiSeries(
        val categories: List<String>,
        val series: List<MultiSeriesItem>,
    )

    private companion object {
        val BASE_IMPORTS =
            listOf(
                "import androidx.compose.runtime.Composable",
                "import io.github.dautovicharis.charts.StackedAreaChart",
                "import io.github.dautovicharis.charts.model.toMultiChartDataSet",
            )
        const val STYLE_IMPORT = "import io.github.dautovicharis.charts.style.StackedAreaChartDefaults"
        const val COMPONENT_NAME = "StackedAreaChart"
        const val STYLE_BUILDER = "StackedAreaChartDefaults.style"
    }
}
