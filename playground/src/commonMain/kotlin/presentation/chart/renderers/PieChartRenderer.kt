package presentation.chart.renderers

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ValidatedChartSpec
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.pie.PieChart
import presentation.chart.StyleReader
import presentation.chart.pieChartStyle

@Composable
internal fun PieChartRenderer(
    spec: ValidatedChartSpec,
    styleReader: StyleReader,
) {
    val data = spec.data as ChartData.SingleSeries
    val labels = data.labels ?: data.values.indices.map(Int::toString)
    val style = pieChartStyle(styleReader, spec.data)
    PieChart(
        data = data.values.map { value -> value.toDouble() }.toChartData(categories = labels),
        style = style,
        title = spec.title,
    )
}
