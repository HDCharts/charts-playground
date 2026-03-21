package model

enum class ChartType(
    val displayName: String,
    val codegenSuffix: String,
) {
    PIE("Pie", "PieChart"),
    LINE("Line", "LineChart"),
    MULTI_LINE("Multi Line", "MultiLineChart"),
    BAR("Bar", "BarChart"),
    HISTOGRAM("Histogram", "HistogramChart"),
    STACKED_BAR("Stacked Bar", "StackedBarChart"),
    AREA("Area", "AreaChart"),
    RADAR("Radar", "RadarChart"),
}
