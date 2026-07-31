package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.PieStyleDefaults
import domain.PieStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.PieChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun PieChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.SingleSeries
    val styleState = spec.styleState as PieStyleState
    val dataSet = data.values.toChartDataSet(title = spec.title, labels = data.labels)
    val defaultStyle = PieChartDefaults.style()
    val style =
        PieChartDefaults.style(
            donutPercentage = styleState.donutPercentage ?: PieStyleDefaults.donutPercentage,
            borderWidth = styleState.borderWidth ?: PieStyleDefaults.borderWidth,
            pieAlpha = styleState.pieAlpha ?: PieStyleDefaults.pieAlpha,
            legendVisible = styleState.legendVisible ?: PieStyleDefaults.legendVisible,
            pieColors =
                styleState.pieColors?.let { colors ->
                    normalizeColorCount(colors, data.values.size).map { it.toComposeColor() }
                } ?: defaultStyle.pieColors,
            pieColor = defaultStyle.pieColor,
            borderColor = defaultStyle.borderColor,
        )
    PieChart(dataSet = dataSet, style = style)
}
