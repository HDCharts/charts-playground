package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.HistogramChart
import presentation.chart.StyleReader
import presentation.chart.histogramChartStyle
import presentation.chart.toLibraryData

@Composable
internal fun HistogramChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.SingleSeries
    HistogramChart(data = data.toLibraryData(), title = spec.title, style = histogramChartStyle(styleReader, spec.data))
}
