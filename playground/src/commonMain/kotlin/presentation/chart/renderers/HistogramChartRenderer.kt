package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.BarStyleDefaults
import domain.BarStyleState
import domain.ChartData
import domain.ChartSession
import domain.normalizeColorCount
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun HistogramChartRenderer(session: ChartSession) {
    val data = session.data as ChartData.SingleSeries
    val styleState = session.styleState as BarStyleState
    val dataSet = data.values.toChartDataSet(title = session.title, labels = data.labels)
    val defaultStyle = HistogramChartDefaults.style()
    val style =
        HistogramChartDefaults.style(
            barColor = styleState.barColor?.toComposeColor() ?: defaultStyle.barColor,
            barColors =
                styleState.barColors?.let { colors ->
                    normalizeColorCount(colors, data.values.size).map { it.toComposeColor() }
                } ?: defaultStyle.barColors,
            barAlpha = styleState.barAlpha ?: BarStyleDefaults.barAlpha,
            gridVisible = styleState.gridVisible ?: BarStyleDefaults.gridVisible,
            axisVisible = styleState.axisVisible ?: BarStyleDefaults.axisVisible,
            selectionLineVisible = styleState.selectionLineVisible ?: BarStyleDefaults.selectionLineVisible,
            selectionLineWidth = styleState.selectionLineWidth ?: BarStyleDefaults.selectionLineWidth,
            zoomControlsVisible = styleState.zoomControlsVisible ?: BarStyleDefaults.zoomControlsVisible,
        )
    HistogramChart(dataSet = dataSet, style = style)
}
