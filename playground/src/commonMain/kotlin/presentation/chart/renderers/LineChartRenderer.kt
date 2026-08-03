package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.LineStyleDefaults
import domain.LineStyleState
import domain.ValidatedChartSpec
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.LineChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun LineChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.SingleSeries
    val styleState = spec.styleState as LineStyleState
    val dataSet = data.values.toChartDataSet(title = spec.title, labels = data.labels)
    val defaultStyle = LineChartDefaults.style()
    val style =
        LineChartDefaults.style(
            lineColor = styleState.lineColor?.toComposeColor() ?: defaultStyle.lineColor,
            lineAlpha = styleState.lineAlpha ?: LineStyleDefaults.lineAlpha,
            bezier = styleState.bezier ?: LineStyleDefaults.bezier,
            pointColor = styleState.pointColor?.toComposeColor() ?: defaultStyle.pointColor,
            pointVisible = styleState.pointVisible ?: LineStyleDefaults.pointVisible,
            pointSize = styleState.pointSize ?: LineStyleDefaults.pointSize,
            dragPointColor = styleState.dragPointColor?.toComposeColor() ?: defaultStyle.dragPointColor,
            dragPointVisible = styleState.dragPointVisible ?: LineStyleDefaults.dragPointVisible,
            dragPointSize = styleState.dragPointSize ?: LineStyleDefaults.dragPointSize,
            dragActivePointSize = styleState.dragActivePointSize ?: LineStyleDefaults.dragActivePointSize,
            axisVisible = styleState.axisVisible ?: LineStyleDefaults.axisVisible,
            axisLineWidth = styleState.axisLineWidth ?: LineStyleDefaults.axisLineWidth,
            xAxisLabelsVisible = styleState.xAxisLabelsVisible ?: LineStyleDefaults.xAxisLabelsVisible,
            yAxisLabelsVisible = styleState.yAxisLabelsVisible ?: LineStyleDefaults.yAxisLabelsVisible,
            zoomControlsVisible = styleState.zoomControlsVisible ?: LineStyleDefaults.zoomControlsVisible,
        )
    LineChart(dataSet = dataSet, style = style)
}
