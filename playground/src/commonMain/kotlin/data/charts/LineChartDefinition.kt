package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toSingleSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor
import kotlin.math.roundToInt

private const val LATENCY_WINDOW_SIZE = 30

internal object LineChartDefinition : SingleSeriesChart(
    type = ChartType.LINE,
    defaultTitle = "API Latency",
    labelPrefix = "Point",
    randomValues = 110f..200f,
) {
    override fun defaultData(): ChartData {
        val latency = SampleDataSources.latency
        val data =
            latency
                .toSingleDataSet(
                    latency.createSingleWindow(windowSize = LATENCY_WINDOW_SIZE),
                ).toSingleSeries()
        return data.copy(values = data.values.map { value -> value.roundToInt().toFloat() })
    }

    override val settings: List<SettingDescriptor> = lineStyleSettings

    override val styleProfile = StyleCodeProfile("LineChartDefaults")

    override val component = "LineChart"
}
