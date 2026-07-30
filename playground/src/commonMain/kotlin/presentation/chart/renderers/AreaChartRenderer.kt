package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.AreaStyleDefaults
import domain.AreaStyleState
import domain.ChartData
import domain.ChartSession
import domain.normalizeColorCount
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun AreaChartRenderer(session: ChartSession) {
    val data = session.data as ChartData.MultiSeries
    val styleState = session.styleState as AreaStyleState
    val categories = data.xLabels.orEmpty()
    val dataSet =
        data.series
            .map { series -> series.name to series.values }
            .toMultiChartDataSet(title = session.title, categories = categories)

    val defaultStyle = StackedAreaChartDefaults.style()
    val style =
        StackedAreaChartDefaults.style(
            areaColors =
                styleState.areaColors?.let { colors ->
                    normalizeColorCount(colors, data.series.size).map { it.toComposeColor() }
                } ?: defaultStyle.areaColors,
            lineColors =
                styleState.lineColors?.let { colors ->
                    normalizeColorCount(colors, data.series.size).map { it.toComposeColor() }
                } ?: defaultStyle.lineColors,
            fillAlpha = styleState.fillAlpha ?: AreaStyleDefaults.fillAlpha,
            lineVisible = styleState.lineVisible ?: AreaStyleDefaults.lineVisible,
            lineWidth = styleState.lineWidth ?: AreaStyleDefaults.lineWidth,
            bezier = styleState.bezier ?: AreaStyleDefaults.bezier,
            zoomControlsVisible = styleState.zoomControlsVisible ?: AreaStyleDefaults.zoomControlsVisible,
        )
    StackedAreaChart(dataSet = dataSet, style = style)
}
