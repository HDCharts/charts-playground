package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.RadarChart
import presentation.chart.StyleReader
import presentation.chart.radarChartStyle
import presentation.chart.toLibraryData

@Composable
internal fun RadarChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.MultiSeries
    RadarChart(data = data.toLibraryData(), title = spec.title, style = radarChartStyle(styleReader, spec.data))
}
