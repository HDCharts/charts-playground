package data

import codegen.GeneratedArtifact
import domain.ValidatedChartSpec

class ChartCodegenService(
    charts: List<PlaygroundChart> = playgroundCharts,
) {
    private val chartsByType = charts.associateBy(PlaygroundChart::type)

    init {
        require(chartsByType.size == charts.size) { "Chart types must be unique" }
    }

    fun generateArtifact(spec: ValidatedChartSpec): GeneratedArtifact =
        chartsByType.getValue(spec.chartType).generateArtifact(spec)

    fun generate(spec: ValidatedChartSpec): String = generateArtifact(spec).source
}
