package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.MultiLineStyleDefaults
import domain.MultiLineStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.LineChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun MultiLineChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.MultiSeries
    val styleState = spec.styleState as MultiLineStyleState
    val chartData =
        data.series
            .map { series -> series.name to series.values.map(Float::toDouble) }
            .toChartData(categories = data.xLabels.orEmpty())
    val defaultStyle = LineChartDefaults.style()
    val style =
        LineChartDefaults.style(
            line =
                LineChartDefaults.line(
                    colors =
                        styleState.lineColors?.let { colors ->
                            normalizeColorCount(colors, data.series.size).map { it.toComposeColor() }
                        } ?: defaultStyle.line.colors,
                    alpha = styleState.lineAlpha ?: MultiLineStyleDefaults.lineAlpha,
                    bezier = styleState.bezier ?: MultiLineStyleDefaults.bezier,
                    strokeWidth = defaultStyle.line.strokeWidth,
                ),
            points =
                LineChartDefaults.points(
                    visible = styleState.pointVisible ?: MultiLineStyleDefaults.pointVisible,
                    color = styleState.pointColor?.toComposeColor() ?: defaultStyle.points.color,
                ),
            selection =
                LineChartDefaults.selection(
                    visible = styleState.dragPointVisible ?: MultiLineStyleDefaults.dragPointVisible,
                    color = styleState.dragPointColor?.toComposeColor() ?: defaultStyle.selection.color,
                ),
        )
    LineChart(data = chartData, title = spec.title, style = style)
}
