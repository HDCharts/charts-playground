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
import domain.ChartType

val chartCatalog: ChartCatalog =
    ChartCatalog(
        charts =
            listOf(
                LineChartDefinition,
                BarChartDefinition,
                HistogramChartDefinition,
                PieChartDefinition,
                RadarChartDefinition,
                AreaChartDefinition,
                MultiLineChartDefinition,
                StackedBarChartDefinition,
            ),
        // Navigation order: related chart families sit next to each other.
        chartTypes =
            listOf(
                ChartType.LINE,
                ChartType.MULTI_LINE,
                ChartType.AREA,
                ChartType.BAR,
                ChartType.STACKED_BAR,
                ChartType.HISTOGRAM,
                ChartType.PIE,
                ChartType.RADAR,
            ),
    )
