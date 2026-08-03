package data

import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.RowId
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartDataAdaptersTest {
    @Test
    fun randomize_formats_generated_numeric_values_as_whole_numbers() {
        val table =
            DataTableState(
                columns =
                    listOf(
                        DataTableColumn(id = "label", label = "Label", numeric = false),
                        DataTableColumn(id = "value", label = "Value", numeric = true),
                    ),
                rows =
                    listOf(
                        DataTableRow(
                            id = RowId(1),
                            cells = mapOf("label" to "Point 1", "value" to "1"),
                        ),
                    ),
                minRows = 1,
            )

        val randomized = randomizeEditorValues(table) { 9.63151f }

        assertEquals(
            "Point 1",
            randomized.rows
                .single()
                .cells
                .getValue("label"),
        )
        assertEquals(
            "10",
            randomized.rows
                .single()
                .cells
                .getValue("value"),
        )
    }
}
