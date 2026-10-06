package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toSingleSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object PieChartDefinition : SingleSeriesChart(
    type = ChartType.PIE,
    defaultTitle = "Revenue Breakdown",
    labelPrefix = "Slice",
    randomValues = 8f..56f,
    nonNegative = true,
) {
    override fun defaultData(): ChartData =
        SampleDataSources.pie
            .deterministic()
            .data
            .toSingleSeries()

    override val settings: List<SettingDescriptor> = pieStyleSettings

    override val styleProfile = StyleCodeProfile("PieChartDefaults")

    override val component = "PieChart"
}
