package presentation.chart.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import domain.ChartData
import domain.LineStyleDefaults
import domain.LineStyleState
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.LineChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun LineChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.SingleSeries
    val styleState = spec.styleState as LineStyleState
    val chartData = data.values.map(Float::toDouble).toChartData(categories = data.labels.orEmpty())
    val defaultStyle = LineChartDefaults.style()
    val style =
        LineChartDefaults.style(
            line =
                LineChartDefaults.line(
                    color = styleState.lineColor?.toComposeColor() ?: defaultStyle.line.color,
                    alpha = styleState.lineAlpha ?: LineStyleDefaults.lineAlpha,
                    bezier = styleState.bezier ?: LineStyleDefaults.bezier,
                    strokeWidth = defaultStyle.line.strokeWidth,
                ),
            points =
                LineChartDefaults.points(
                    color = styleState.pointColor?.toComposeColor() ?: defaultStyle.points.color,
                    visible = styleState.pointVisible ?: LineStyleDefaults.pointVisible,
                    size = (styleState.pointSize ?: LineStyleDefaults.pointSize).dp,
                ),
            selection =
                LineChartDefaults.selection(
                    color = styleState.dragPointColor?.toComposeColor() ?: defaultStyle.selection.color,
                    visible = styleState.dragPointVisible ?: LineStyleDefaults.dragPointVisible,
                    size = (styleState.dragPointSize ?: LineStyleDefaults.dragPointSize).dp,
                    activeSize = (styleState.dragActivePointSize ?: LineStyleDefaults.dragActivePointSize).dp,
                ),
            axis =
                LineChartDefaults.axis(
                    visible = styleState.axisVisible ?: LineStyleDefaults.axisVisible,
                    lineWidth = (styleState.axisLineWidth ?: LineStyleDefaults.axisLineWidth).dp,
                    xLabels =
                        LineChartDefaults.xLabels(
                            visible = styleState.xAxisLabelsVisible ?: LineStyleDefaults.xAxisLabelsVisible,
                        ),
                    yLabels =
                        LineChartDefaults.yLabels(
                            visible = styleState.yAxisLabelsVisible ?: LineStyleDefaults.yAxisLabelsVisible,
                        ),
                ),
            zoomControlsVisible = styleState.zoomControlsVisible ?: LineStyleDefaults.zoomControlsVisible,
        )
    LineChart(data = chartData, title = spec.title, style = style)
}
