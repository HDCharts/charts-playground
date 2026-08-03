package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.StackedBarStyleDefaults
import domain.StackedBarStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.StackedBarChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun StackedBarChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.StackedSeries
    val styleState = spec.styleState as StackedBarStyleState
    val series =
        data.segmentNames.mapIndexed { segmentIndex, name ->
            name to data.bars.map { bar -> bar.values.getOrElse(segmentIndex) { 0f } }
        }
    val dataSet =
        series.toMultiChartDataSet(
            title = spec.title,
            categories = data.bars.map { bar -> bar.label },
            prefix = "$",
        )
    val defaultStyle = StackedBarChartDefaults.style()
    val style =
        StackedBarChartDefaults.style(
            barColors =
                styleState.barColors?.let { colors ->
                    normalizeColorCount(colors, data.segmentNames.size).map { it.toComposeColor() }
                } ?: defaultStyle.barColors,
            barAlpha = styleState.barAlpha ?: StackedBarStyleDefaults.barAlpha,
            selectionLineVisible =
                styleState.selectionLineVisible ?: StackedBarStyleDefaults.selectionLineVisible,
            selectionLineWidth = styleState.selectionLineWidth ?: StackedBarStyleDefaults.selectionLineWidth,
            zoomControlsVisible = styleState.zoomControlsVisible ?: StackedBarStyleDefaults.zoomControlsVisible,
        )
    StackedBarChart(dataSet = dataSet, style = style)
}
