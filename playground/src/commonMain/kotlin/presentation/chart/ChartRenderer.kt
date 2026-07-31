package presentation.chart

import androidx.compose.runtime.Composable
import domain.ChartType
import domain.ValidatedChartSpec
import presentation.chart.renderers.AreaChartRenderer
import presentation.chart.renderers.BarChartRenderer
import presentation.chart.renderers.HistogramChartRenderer
import presentation.chart.renderers.LineChartRenderer
import presentation.chart.renderers.MultiLineChartRenderer
import presentation.chart.renderers.PieChartRenderer
import presentation.chart.renderers.RadarChartRenderer
import presentation.chart.renderers.StackedBarChartRenderer

@Composable
internal fun ChartRenderer(
    type: ChartType,
    spec: ValidatedChartSpec,
) {
    when (type) {
        ChartType.PIE -> PieChartRenderer(spec)
        ChartType.LINE -> LineChartRenderer(spec)
        ChartType.MULTI_LINE -> MultiLineChartRenderer(spec)
        ChartType.BAR -> BarChartRenderer(spec)
        ChartType.HISTOGRAM -> HistogramChartRenderer(spec)
        ChartType.STACKED_BAR -> StackedBarChartRenderer(spec)
        ChartType.AREA -> AreaChartRenderer(spec)
        ChartType.RADAR -> RadarChartRenderer(spec)
    }
}
