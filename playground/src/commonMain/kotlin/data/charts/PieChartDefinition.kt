package data.charts

import codegen.SnippetData
import codegen.common.StyleCodeProfile
import data.SampleDataSources
import data.toSingleSeries
import domain.ChartData
import domain.ChartType
import domain.PIE_SLICE_COLORS_PATH
import domain.SettingDescriptor
import domain.StyleValue
import domain.ValidatedChartSpec
import domain.normalizeColorCount

internal object PieChartDefinition : SingleSeriesChart(
    type = ChartType.PIE,
    defaultTitle = "Revenue Breakdown",
    labelPrefix = "Slice",
    randomValues = 8f..56f,
    nonNegative = true,
) {
    override fun defaultData(): ChartData =
        SampleDataSources.pie
            .initialPieSample()
            .slices
            .toSingleSeries()

    override val settings: List<SettingDescriptor> = pieStyleSettings

    override val styleProfile = StyleCodeProfile("PieChartDefaults")

    override val component = "PieChart"

    /** Slice colors are set on the data rows, as the preview does. */
    override fun snippetData(spec: ValidatedChartSpec): SnippetData {
        val data = spec.data as ChartData.SingleSeries
        val labels = data.labels ?: data.values.indices.map(Int::toString)
        val colors =
            (spec.styleState[PIE_SLICE_COLORS_PATH] as StyleValue.Colors?)
                ?.value
                ?.let { normalizeColorCount(it, data.values.size) }
        return SnippetData.Slices(
            data.values.mapIndexed { index, value ->
                SnippetData.Slices.Slice(label = labels[index], value = value, color = colors?.getOrNull(index))
            },
        )
    }
}
