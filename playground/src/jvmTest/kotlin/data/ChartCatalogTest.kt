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
            chartCatalog.chartTypes,
        )
    }

    @Test
    fun catalog_rejects_duplicate_chart_types() {
        assertFailsWith<IllegalArgumentException> {
            chartCatalog.copy(charts = chartCatalog.charts + chartCatalog.charts.first())
        }
    }

    @Test
    fun catalog_rejects_duplicate_navigation_types() {
        assertFailsWith<IllegalArgumentException> {
            chartCatalog.copy(chartTypes = chartCatalog.chartTypes + ChartType.LINE)
        }
    }

    @Test
    fun catalog_rejects_navigation_types_missing_a_chart() {
        assertFailsWith<IllegalArgumentException> {
            chartCatalog.copy(chartTypes = chartCatalog.chartTypes - ChartType.HISTOGRAM)
        }
    }
}
