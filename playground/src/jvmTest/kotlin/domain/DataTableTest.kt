package domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DataTableTest {
    private val table =
        DataTableState(
            columns =
                listOf(
                    DataTableColumn(id = "label", label = "Label", numeric = false),
                    DataTableColumn(id = "value", label = "Value", numeric = true),
                ),
            rows =
                listOf(
                    DataTableRow(id = 1, cells = mapOf("label" to "A", "value" to "1")),
                    DataTableRow(id = 2, cells = mapOf("label" to "B", "value" to "2")),
                ),
            minRows = 1,
        )

    @Test
    fun updateCell_changes_only_the_requested_cell() {
        val updated = table.updateCell(rowIndex = 1, columnId = "value", value = "3")

        assertEquals("3", updated.rows[1].cells.getValue("value"))
        assertEquals("A", updated.rows[0].cells.getValue("label"))
    }

    @Test
    fun updateCell_ignores_invalid_row_and_column() {
        assertSame(table, table.updateCell(rowIndex = -1, columnId = "value", value = "3"))
        assertSame(table, table.updateCell(rowIndex = 0, columnId = "unknown", value = "3"))
    }

    @Test
    fun withDeletedRow_keeps_the_configured_minimum() {
        val oneRow = table.withDeletedRow(0)

        assertEquals(1, oneRow.rows.size)
        assertSame(oneRow, oneRow.withDeletedRow(0))
        assertSame(oneRow, oneRow.withDeletedRow(99))
    }

    @Test
    fun withAddedRow_uses_a_new_id_after_the_current_maximum() {
        val updated = table.withAddedRow(mapOf("label" to "C", "value" to "3"))

        assertEquals(3, updated.rows.last().id)
        assertEquals(
            "C",
            updated.rows
                .last()
                .cells
                .getValue("label"),
        )
    }
}
