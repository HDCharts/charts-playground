package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toMultiSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object RadarChartDefinition : MultiSeriesChart(
    type = ChartType.RADAR,
    defaultTitle = "Platform Capability",
    labelPrefix = "Axis",
    randomValues = 35f..100f,
    nonNegative = true,
    labelHeader = "Axis",
    minRows = 3,
) {
    override fun defaultData(): ChartData =
        SampleDataSources.radar
            .initialRadarSample()
            .customData
            .toMultiSeries(labelPrefix)

    override val settings: List<SettingDescriptor> = radarStyleSettings

    override val styleProfile = StyleCodeProfile("RadarChartDefaults")

    override val component = "RadarChart"
}
