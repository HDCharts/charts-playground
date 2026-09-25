package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.StackedBarChart
import presentation.chart.StyleReader
import presentation.chart.stackedBarChartStyle
import presentation.chart.toLibraryData

@Composable
internal fun StackedBarChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.MultiSeries
    StackedBarChart(
        data = data.toLibraryData(),
        title = spec.title,
        style = stackedBarChartStyle(styleReader, spec.data),
    )
}
