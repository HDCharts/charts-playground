package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toMultiSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object StackedBarChartDefinition : MultiSeriesChart(
    type = ChartType.STACKED_BAR,
    defaultTitle = "Quarterly Revenue Mix",
    labelPrefix = "Bar",
    randomValues = 30f..210f,
    nonNegative = true,
) {
    override fun defaultData(): ChartData =
        SampleDataSources.stackedBar
            .initialStackedBarSample()
            .dataSet
            .toMultiSeries(labelPrefix)

    override val settings: List<SettingDescriptor> = stackedBarStyleSettings

    override val styleProfile = StyleCodeProfile("StackedBarChartDefaults")

    override val component = "StackedBarChart"
}
