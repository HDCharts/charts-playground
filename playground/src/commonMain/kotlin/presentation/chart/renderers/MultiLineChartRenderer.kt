package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.LineChart
import presentation.chart.StyleReader
import presentation.chart.lineChartStyle
import presentation.chart.toLibraryData

@Composable
internal fun MultiLineChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.MultiSeries
    LineChart(data = data.toLibraryData(), title = spec.title, style = lineChartStyle(styleReader, spec.data))
}
