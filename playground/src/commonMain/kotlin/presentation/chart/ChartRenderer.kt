package presentation.chart

import androidx.compose.runtime.Composable
import domain.ChartType
import domain.SettingDescriptor
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
    settings: List<SettingDescriptor>,
) {
    val styleReader = StyleReader.active(settings, spec.styleState)
    when (type) {
        ChartType.PIE -> PieChartRenderer(spec, styleReader)
        ChartType.LINE -> LineChartRenderer(spec, styleReader)
        ChartType.MULTI_LINE -> MultiLineChartRenderer(spec, styleReader)
        ChartType.BAR -> BarChartRenderer(spec, styleReader)
        ChartType.HISTOGRAM -> HistogramChartRenderer(spec, styleReader)
        ChartType.STACKED_BAR -> StackedBarChartRenderer(spec, styleReader)
        ChartType.AREA -> AreaChartRenderer(spec, styleReader)
        ChartType.RADAR -> RadarChartRenderer(spec, styleReader)
    }
}
