package presentation.chart.renderers

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import domain.ChartData
import domain.StackedBarStyleDefaults
import domain.StackedBarStyleState
import domain.ValidatedChartSpec
import domain.normalizeColorCount
import io.github.hdcharts.charts.StackedBarChart
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.style.StackedBarChartDefaults
import presentation.colors.toComposeColor
import io.github.hdcharts.charts.model.ChartData as LibraryChartData

@Composable
internal fun StackedBarChartRenderer(spec: ValidatedChartSpec) {
    val data = spec.data as ChartData.StackedSeries
    val styleState = spec.styleState as StackedBarStyleState
    val chartData =
        LibraryChartData(
            categories = data.bars.map { bar -> bar.label },
            series =
                data.segmentNames.mapIndexed { segmentIndex, name ->
                    ChartSeries(
                        name = name,
                        values = data.bars.map { bar -> bar.values.getOrElse(segmentIndex) { 0f }.toDouble() },
                    )
                },
        )
    val defaultStyle = StackedBarChartDefaults.style()
    val style =
        StackedBarChartDefaults.style(
            segments =
                StackedBarChartDefaults.segments(
                    colors =
                        styleState.barColors?.let { colors ->
                            normalizeColorCount(colors, data.segmentNames.size).map { it.toComposeColor() }
                        } ?: defaultStyle.segments.colors,
                    color = defaultStyle.segments.color,
                    alpha = styleState.barAlpha ?: StackedBarStyleDefaults.barAlpha,
                ),
            selection =
                StackedBarChartDefaults.selection(
                    visible = styleState.selectionLineVisible ?: StackedBarStyleDefaults.selectionLineVisible,
                    width = (styleState.selectionLineWidth ?: StackedBarStyleDefaults.selectionLineWidth).dp,
                ),
            zoomControlsVisible = styleState.zoomControlsVisible ?: StackedBarStyleDefaults.zoomControlsVisible,
        )
    StackedBarChart(data = chartData, title = spec.title, style = style)
}
