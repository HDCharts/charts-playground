package data

import data.charts.AreaChartDefinition
import data.charts.BarChartDefinition
import data.charts.HistogramChartDefinition
import data.charts.LineChartDefinition
import data.charts.MultiLineChartDefinition
import data.charts.PieChartDefinition
import data.charts.RadarChartDefinition
import data.charts.StackedBarChartDefinition
import domain.ChartCatalog

/** Every chart, in navigation order: related chart families sit next to each other. */
internal val playgroundCharts: List<PlaygroundChart> =
    listOf(
        LineChartDefinition,
        MultiLineChartDefinition,
        AreaChartDefinition,
        BarChartDefinition,
        StackedBarChartDefinition,
        HistogramChartDefinition,
        PieChartDefinition,
        RadarChartDefinition,
    )

val chartCatalog: ChartCatalog =
    ChartCatalog(
        charts = playgroundCharts,
        chartTypes = playgroundCharts.map { it.type },
    )
