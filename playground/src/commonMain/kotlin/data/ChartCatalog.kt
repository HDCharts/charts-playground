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
        primaryChartTypes =
            listOf(
                ChartType.LINE,
                ChartType.BAR,
                ChartType.PIE,
                ChartType.RADAR,
                ChartType.AREA,
            ),
        overflowChartTypes =
            listOf(
                ChartType.MULTI_LINE,
                ChartType.HISTOGRAM,
                ChartType.STACKED_BAR,
            ),
    )
