package data.charts

import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toSingleSeries
import domain.ChartData
import domain.ChartType
import domain.SettingDescriptor

internal object HistogramChartDefinition : SingleSeriesChart(
    type = ChartType.HISTOGRAM,
    defaultTitle = "Request Duration Distribution",
    labelPrefix = "Bin",
    randomValues = 0f..80f,
    nonNegative = true,
    labelHeader = "Bin",
) {
    override fun defaultData(): ChartData = SampleDataSources.histogram.initialHistogramDataSet().toSingleSeries()

    override val settings: List<SettingDescriptor> = histogramStyleSettings

    override val styleProfile =
        StyleCodeProfile(
            styleObject = "HistogramChartDefaults",
            // HistogramChartDefaults only builds bars; the other blocks are shared with bar charts.
            blockOwners = listOf("range", "grid", "axis", "selectionLine").associateWith { "BarChartDefaults" },
            // HistogramChartDefaults.style() starts the range at 0; BarChartDefaults.range() does not.
            blockDefaults = mapOf("range" to mapOf("min" to "0.0")),
        )

    override val component = "HistogramChart"
}
