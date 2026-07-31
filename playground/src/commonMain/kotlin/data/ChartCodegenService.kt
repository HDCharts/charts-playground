package data

import codegen.GeneratedArtifact
import data.charts.AreaChartDefinition
import data.charts.BarChartDefinition
import data.charts.HistogramChartDefinition
import data.charts.LineChartDefinition
import data.charts.MultiLineChartDefinition
import data.charts.PieChartDefinition
import data.charts.RadarChartDefinition
import data.charts.StackedBarChartDefinition
import domain.ChartType
import domain.ValidatedChartSpec

interface ChartCodegenAdapter {
    val type: ChartType

    fun generate(spec: ValidatedChartSpec): String

    fun generateArtifact(spec: ValidatedChartSpec): GeneratedArtifact = GeneratedArtifact(source = generate(spec))
}

class ChartCodegenService(
    adapters: List<ChartCodegenAdapter> = defaultChartCodegenAdapters,
) {
    private val adaptersByType = adapters.associateBy(ChartCodegenAdapter::type)

    init {
        require(adaptersByType.size == adapters.size) {
            "Chart codegen adapters must have unique chart types"
        }
    }

    fun generateArtifact(spec: ValidatedChartSpec): GeneratedArtifact =
        adaptersByType.getValue(spec.chartType).generateArtifact(spec)

    fun generate(spec: ValidatedChartSpec): String = generateArtifact(spec).source
}

private val defaultChartCodegenAdapters: List<ChartCodegenAdapter> =
    listOf(
        AreaChartDefinition,
        BarChartDefinition,
        HistogramChartDefinition,
        LineChartDefinition,
        MultiLineChartDefinition,
        PieChartDefinition,
        RadarChartDefinition,
        StackedBarChartDefinition,
    )
