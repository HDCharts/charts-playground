package data

import domain.ChartType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChartCatalogTest {
    @Test
    fun catalog_has_all_chart_types_once_and_deterministic_order() {
        val types = chartCatalog.charts.map { definition -> definition.type }

        assertEquals(ChartType.entries.toSet(), types.toSet())
        assertEquals(types.size, types.toSet().size)
        assertEquals(
            listOf(ChartType.LINE, ChartType.BAR, ChartType.PIE, ChartType.RADAR, ChartType.AREA),
            chartCatalog.primaryChartTypes,
        )
        assertEquals(
            listOf(ChartType.MULTI_LINE, ChartType.HISTOGRAM, ChartType.STACKED_BAR),
            chartCatalog.overflowChartTypes,
        )
    }

    @Test
    fun catalog_rejects_duplicate_chart_types() {
        assertFailsWith<IllegalArgumentException> {
            chartCatalog.copy(charts = chartCatalog.charts + chartCatalog.charts.first())
        }
    }

    @Test
    fun catalog_rejects_duplicate_primary_types() {
        assertFailsWith<IllegalArgumentException> {
            chartCatalog.copy(primaryChartTypes = chartCatalog.primaryChartTypes + ChartType.LINE)
        }
    }

    @Test
    fun catalog_rejects_duplicate_overflow_types() {
        assertFailsWith<IllegalArgumentException> {
            chartCatalog.copy(overflowChartTypes = chartCatalog.overflowChartTypes + ChartType.HISTOGRAM)
        }
    }
}
