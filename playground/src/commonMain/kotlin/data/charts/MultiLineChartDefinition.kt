package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toMultiSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object MultiLineChartDefinition : MultiSeriesChart(
    type = ChartType.MULTI_LINE,
    defaultTitle = "Revenue By Channel",
    labelPrefix = "Point",
    randomValues = 220f..740f,
) {
    override fun defaultData(): ChartData =
        SampleDataSources.multiLine
            .initialMultiLineSample()
            .dataSet
            .toMultiSeries(labelPrefix)

    override val settings: List<SettingDescriptor> = multiLineStyleSettings

    override val styleProfile = StyleCodeProfile("LineChartDefaults")

    override val component = "LineChart"
}
