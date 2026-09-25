package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toSingleSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object LineChartDefinition : SingleSeriesChart(
    type = ChartType.LINE,
    defaultTitle = "Monthly Trend",
    labelPrefix = "Point",
    randomValues = 8f..44f,
) {
    override fun defaultData(): ChartData = SampleDataSources.line.initialLineDataSet().toSingleSeries()

    override val settings: List<SettingDescriptor> = lineStyleSettings

    override val styleProfile = StyleCodeProfile("LineChartDefaults")

    override val component = "LineChart"
}
