package domain

data class ChartCatalog(
    val charts: List<ChartDefinition>,
    val primaryChartTypes: List<ChartType>,
    val overflowChartTypes: List<ChartType>,
) {
    private val byType: Map<ChartType, ChartDefinition> = charts.associateBy { chart -> chart.type }

    init {
        require(charts.isNotEmpty()) { "At least one chart definition is required" }
        require(byType.size == charts.size) { "Chart types must be unique" }
        require(primaryChartTypes.size == primaryChartTypes.toSet().size) {
            "Primary chart types must be unique"
        }
        require(overflowChartTypes.size == overflowChartTypes.toSet().size) {
            "Overflow chart types must be unique"
        }
        require(primaryChartTypes.toSet().intersect(overflowChartTypes.toSet()).isEmpty()) {
            "Primary and overflow chart types must not overlap"
        }
        require(primaryChartTypes.toSet() + overflowChartTypes.toSet() == byType.keys) {
            "Primary and overflow chart types must cover all chart definitions"
        }
    }

    fun definition(type: ChartType): ChartDefinition =
        byType[type]
            ?: error("Missing chart definition for $type")
}
