package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toSingleSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object BarChartDefinition : SingleSeriesChart(
    type = ChartType.BAR,
    defaultTitle = "Weekly Performance",
    labelPrefix = "Bar",
    randomValues = -25f..60f,
) {
    override fun defaultData(): ChartData = SampleDataSources.bar.initialBarDataSet().toSingleSeries()

    override val settings: List<SettingDescriptor> = barStyleSettings

    override val styleProfile = StyleCodeProfile("BarChartDefaults")

    override val component = "BarChart"
}
