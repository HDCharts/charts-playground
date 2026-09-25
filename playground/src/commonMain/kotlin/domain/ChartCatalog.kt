package domain

data class ChartCatalog(
    val charts: List<ChartDefinition>,
    val chartTypes: List<ChartType>,
) {
    private val byType: Map<ChartType, ChartDefinition> = charts.associateBy { chart -> chart.type }

    init {
        require(charts.isNotEmpty()) { "At least one chart definition is required" }
        require(byType.size == charts.size) { "Chart types must be unique" }
        require(chartTypes.size == chartTypes.toSet().size) {
            "Navigation chart types must be unique"
        }
        require(chartTypes.toSet() == byType.keys) {
            "Navigation chart types must cover all chart definitions"
        }
    }

    fun definition(type: ChartType): ChartDefinition =
        byType[type]
            ?: error("Missing chart definition for $type")
}
