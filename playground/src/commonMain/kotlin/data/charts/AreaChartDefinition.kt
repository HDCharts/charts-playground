package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toMultiSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object AreaChartDefinition : MultiSeriesChart(
    type = ChartType.AREA,
    defaultTitle = "Plan Distribution",
    labelPrefix = "Point",
    randomValues = 60f..800f,
    nonNegative = true,
) {
    override fun defaultData(): ChartData =
        SampleDataSources.stackedArea
            .initialStackedAreaSample()
            .data
            .toMultiSeries(labelPrefix)

    override val settings: List<SettingDescriptor> = areaStyleSettings

    override val styleProfile = StyleCodeProfile("StackedAreaChartDefaults")

    override val component = "StackedAreaChart"
}
