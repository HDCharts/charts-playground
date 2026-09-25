package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.StackedAreaChart
import presentation.chart.StyleReader
import presentation.chart.areaChartStyle
import presentation.chart.toLibraryData

@Composable
internal fun AreaChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.MultiSeries
    StackedAreaChart(data = data.toLibraryData(), title = spec.title, style = areaChartStyle(styleReader, spec.data))
}
