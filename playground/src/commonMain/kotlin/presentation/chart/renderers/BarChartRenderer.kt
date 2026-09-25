package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.BarChart
import presentation.chart.StyleReader
import presentation.chart.barChartStyle
import presentation.chart.toLibraryData

@Composable
internal fun BarChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.SingleSeries
    BarChart(data = data.toLibraryData(), title = spec.title, style = barChartStyle(styleReader, spec.data))
}
