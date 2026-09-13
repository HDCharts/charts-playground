package presentation.chart.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import domain.AreaStyleDefaults
import domain.AreaStyleState
import domain.ChartData
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.model.toChartData
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import presentation.colors.toComposeColor

@Composable
internal fun AreaChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.MultiSeries
    val styleState = spec.styleState as AreaStyleState
    val categories = data.xLabels.orEmpty()
    val chartData =
        data.series
            .map { series -> series.name to series.values.map(Float::toDouble) }
            .toChartData(categories = categories)

    val defaultStyle = StackedAreaChartDefaults.style()
    val style =
        StackedAreaChartDefaults.style(
            fill =
                StackedAreaChartDefaults.fill(
                    color = defaultStyle.fill.color,
                    colors =
                        styleState.areaColors?.let { colors ->
                            normalizeColorCount(colors, data.series.size).map { it.toComposeColor() }
                        } ?: defaultStyle.fill.colors,
                    alpha = styleState.fillAlpha ?: AreaStyleDefaults.fillAlpha,
                ),
            boundary =
                StackedAreaChartDefaults.boundary(
                    color = defaultStyle.boundary.color,
                    colors =
                        styleState.lineColors?.let { colors ->
                            normalizeColorCount(colors, data.series.size).map { it.toComposeColor() }
                        } ?: defaultStyle.boundary.colors,
                    visible = styleState.lineVisible ?: AreaStyleDefaults.lineVisible,
                    width = (styleState.lineWidth ?: AreaStyleDefaults.lineWidth).dp,
                    bezier = styleState.bezier ?: AreaStyleDefaults.bezier,
                ),
            zoomControlsVisible = styleState.zoomControlsVisible ?: AreaStyleDefaults.zoomControlsVisible,
        )
    StackedAreaChart(data = chartData, title = spec.title, style = style)
}
