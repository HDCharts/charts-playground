package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.RadarStyleDefaults
import domain.RadarStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.RadarChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun RadarChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.RadarSeries
    val styleState = spec.styleState as RadarStyleState
    val chartData =
        data.entries
            .map { entry -> entry.name to entry.values.map(Float::toDouble) }
            .toChartData(categories = data.axes)
    val defaultStyle = RadarChartDefaults.style()
    val style =
        RadarChartDefaults.style(
            polygon =
                RadarChartDefaults.polygon(
                    lineColors =
                        styleState.lineColors?.let { colors ->
                            normalizeColorCount(colors, data.entries.size).map { it.toComposeColor() }
                        } ?: defaultStyle.polygon.lineColors,
                    lineColor = defaultStyle.polygon.lineColor,
                    lineWidth = styleState.lineWidth ?: RadarStyleDefaults.lineWidth,
                    fillVisible = styleState.fillVisible ?: RadarStyleDefaults.fillVisible,
                    fillAlpha = styleState.fillAlpha ?: RadarStyleDefaults.fillAlpha,
                ),
            points =
                RadarChartDefaults.points(
                    visible = styleState.pointVisible ?: RadarStyleDefaults.pointVisible,
                    size = styleState.pointSize ?: RadarStyleDefaults.pointSize,
                    color = defaultStyle.points.color,
                    colorSameAsLine = defaultStyle.points.colorSameAsLine,
                ),
            grid =
                RadarChartDefaults.grid(
                    visible = styleState.gridVisible ?: RadarStyleDefaults.gridVisible,
                ),
            categories =
                RadarChartDefaults.categories(
                    legendVisible = styleState.categoryLegendVisible ?: RadarStyleDefaults.categoryLegendVisible,
                ),
        )
    RadarChart(data = chartData, title = spec.title, style = style)
}
