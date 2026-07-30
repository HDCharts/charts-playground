package data

import data.charts.AreaChartDefinition
import data.charts.BarChartDefinition
import data.charts.HistogramChartDefinition
import data.charts.LineChartDefinition
import data.charts.MultiLineChartDefinition
import data.charts.PieChartDefinition
import data.charts.RadarChartDefinition
import data.charts.StackedBarChartDefinition
import domain.ChartSession
import domain.ChartType

interface ChartCodegenAdapter {
    val type: ChartType

    fun generate(session: ChartSession): String
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

    fun generate(session: ChartSession): String = adaptersByType.getValue(session.chartType).generate(session)
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
