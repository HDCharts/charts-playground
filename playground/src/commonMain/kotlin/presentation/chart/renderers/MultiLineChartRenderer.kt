package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.MultiLineStyleDefaults
import domain.MultiLineStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.LineChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun MultiLineChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.MultiSeries
    val styleState = spec.styleState as MultiLineStyleState
    val dataSet =
        data.series
            .map { series -> series.name to series.values }
            .toMultiChartDataSet(
                title = spec.title,
                categories = data.xLabels.orEmpty(),
                prefix = "$",
            )
    val defaultStyle = LineChartDefaults.style()
    val style =
        LineChartDefaults.style(
            lineColors =
                styleState.lineColors?.let { colors ->
                    normalizeColorCount(colors, data.series.size).map { it.toComposeColor() }
                } ?: defaultStyle.lineColors,
            lineAlpha = styleState.lineAlpha ?: MultiLineStyleDefaults.lineAlpha,
            bezier = styleState.bezier ?: MultiLineStyleDefaults.bezier,
            pointVisible = styleState.pointVisible ?: MultiLineStyleDefaults.pointVisible,
            dragPointVisible = styleState.dragPointVisible ?: MultiLineStyleDefaults.dragPointVisible,
            pointColor = styleState.pointColor?.toComposeColor() ?: defaultStyle.pointColor,
            dragPointColor = styleState.dragPointColor?.toComposeColor() ?: defaultStyle.dragPointColor,
        )
    LineChart(dataSet = dataSet, style = style)
}
