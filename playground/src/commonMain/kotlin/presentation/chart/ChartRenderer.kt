package presentation.chart

import androidx.compose.runtime.Composable
import domain.ChartSession
import domain.ChartType
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
    session: ChartSession,
) {
    when (type) {
        ChartType.PIE -> PieChartRenderer(session)
        ChartType.LINE -> LineChartRenderer(session)
        ChartType.MULTI_LINE -> MultiLineChartRenderer(session)
        ChartType.BAR -> BarChartRenderer(session)
        ChartType.HISTOGRAM -> HistogramChartRenderer(session)
        ChartType.STACKED_BAR -> StackedBarChartRenderer(session)
        ChartType.AREA -> AreaChartRenderer(session)
        ChartType.RADAR -> RadarChartRenderer(session)
    }
}
