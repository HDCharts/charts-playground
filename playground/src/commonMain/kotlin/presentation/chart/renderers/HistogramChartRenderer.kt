package presentation.chart.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import domain.BarStyleDefaults
import domain.BarStyleState
import domain.ChartData
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.hdcharts.charts.HistogramChart
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.BarChartDefaults
import io.github.hdcharts.charts.style.HistogramChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun HistogramChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.SingleSeries
    val styleState = spec.styleState as BarStyleState
    val chartData = data.values.map(Float::toDouble).toChartData(categories = data.labels.orEmpty())
    val defaultStyle = HistogramChartDefaults.style()
    val style =
        HistogramChartDefaults.style(
            bars =
                HistogramChartDefaults.bars(
                    color = styleState.barColor?.toComposeColor() ?: defaultStyle.bars.color,
                    colors =
                        styleState.barColors?.let { colors ->
                            normalizeColorCount(colors, data.values.size).map { it.toComposeColor() }
                        } ?: defaultStyle.bars.colors,
                    alpha = styleState.barAlpha ?: BarStyleDefaults.barAlpha,
                    space = defaultStyle.bars.space,
                    minBarWidth = defaultStyle.bars.minBarWidth,
                ),
            grid =
                BarChartDefaults.grid(
                    visible = styleState.gridVisible ?: BarStyleDefaults.gridVisible,
                ),
            axis =
                BarChartDefaults.axis(
                    visible = styleState.axisVisible ?: BarStyleDefaults.axisVisible,
                ),
            selectionLine =
                BarChartDefaults.selectionLine(
                    visible = styleState.selectionLineVisible ?: BarStyleDefaults.selectionLineVisible,
                    width = (styleState.selectionLineWidth ?: BarStyleDefaults.selectionLineWidth).dp,
                ),
            zoomControlsVisible = styleState.zoomControlsVisible ?: BarStyleDefaults.zoomControlsVisible,
        )
    HistogramChart(data = chartData, title = spec.title, style = style)
}
