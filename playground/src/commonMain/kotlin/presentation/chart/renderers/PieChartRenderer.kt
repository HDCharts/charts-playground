package presentation.chart.renderers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import domain.ChartData
import domain.PieStyleDefaults
import domain.PieStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.model.PieSlice
import io.github.hdcharts.charts.style.PieChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun PieChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.SingleSeries
    val styleState = spec.styleState as PieStyleState
    val slices =
        remember(data.values, data.labels, styleState.pieColors) {
            val labels = data.labels ?: data.values.indices.map(Int::toString)
            val palette = styleState.pieColors?.let { normalizeColorCount(it, data.values.size) }
            data.values.mapIndexed { index, value ->
                PieSlice(
                    label = labels[index],
                    value = value.toDouble(),
                    color = palette?.getOrNull(index)?.toComposeColor(),
                )
            }
        }
    val defaultStyle = PieChartDefaults.style()
    val style =
        PieChartDefaults.style(
            donut =
                PieChartDefaults.donut(
                    holePercentage = styleState.donutPercentage ?: PieStyleDefaults.donutPercentage,
                ),
            slices =
                PieChartDefaults.slices(
                    alpha = styleState.pieAlpha ?: PieStyleDefaults.pieAlpha,
                    baseColor = defaultStyle.slices.baseColor,
                ),
            border =
                PieChartDefaults.border(
                    color = defaultStyle.border.color,
                    width = (styleState.borderWidth ?: PieStyleDefaults.borderWidth).dp,
                ),
            legend =
                PieChartDefaults.legend(
                    visible = styleState.legendVisible ?: PieStyleDefaults.legendVisible,
                ),
        )
    PieChart(data = slices, style = style, title = spec.title)
}
