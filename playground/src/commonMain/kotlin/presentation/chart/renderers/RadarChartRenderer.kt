package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.RadarStyleDefaults
import domain.RadarStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.dautovicharis.charts.RadarChart
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.RadarChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun RadarChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.RadarSeries
    val styleState = spec.styleState as RadarStyleState
    val dataSet =
        data.entries
            .map { entry -> entry.name to entry.values }
            .toMultiChartDataSet(title = spec.title, categories = data.axes)
    val defaultStyle = RadarChartDefaults.style()
    val style =
        RadarChartDefaults.style(
            lineColors =
                styleState.lineColors?.let { colors ->
                    normalizeColorCount(colors, data.entries.size).map { it.toComposeColor() }
                } ?: defaultStyle.lineColors,
            lineWidth = styleState.lineWidth ?: RadarStyleDefaults.lineWidth,
            pointVisible = styleState.pointVisible ?: RadarStyleDefaults.pointVisible,
            pointSize = styleState.pointSize ?: RadarStyleDefaults.pointSize,
            fillVisible = styleState.fillVisible ?: RadarStyleDefaults.fillVisible,
            fillAlpha = styleState.fillAlpha ?: RadarStyleDefaults.fillAlpha,
            gridVisible = styleState.gridVisible ?: RadarStyleDefaults.gridVisible,
            categoryLegendVisible =
                styleState.categoryLegendVisible ?: RadarStyleDefaults.categoryLegendVisible,
        )
    RadarChart(dataSet = dataSet, style = style)
}
