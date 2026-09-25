package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.PIE_SLICE_COLORS_PATH
import domain.ValidatedChartSpec
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.model.PieSlice
import presentation.chart.StyleReader
import presentation.chart.pieChartStyle

@Composable
internal fun PieChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.SingleSeries
    val slices =
        run {
            val labels = data.labels ?: data.values.indices.map(Int::toString)
            val palette = styleReader.colors(PIE_SLICE_COLORS_PATH, data.values.size, default = emptyList())
            data.values.mapIndexed { index, value ->
                PieSlice(label = labels[index], value = value.toDouble(), color = palette.getOrNull(index))
            }
        }
    val style = pieChartStyle(styleReader)
    PieChart(data = slices, style = style, title = spec.title)
}
