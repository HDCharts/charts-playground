package domain

data class DataTableColumn(
    val id: String,
    val label: String,
    val numeric: Boolean,
    val weight: Float = 1f,
    val defaultValue: String = "",
)

data class DataTableRow(
    val id: Int,
    val cells: Map<String, String>,
)

data class DataTableState(
    val columns: List<DataTableColumn>,
    val rows: List<DataTableRow>,
    val minRows: Int,
)

fun DataTableState.updateCell(
    rowIndex: Int,
    columnId: String,
    value: String,
): DataTableState {
    if (rowIndex !in rows.indices) return this
    if (columns.none { column -> column.id == columnId }) return this
    val nextRows =
        rows.toMutableList().also { mutableRows ->
            val row = mutableRows[rowIndex]
            mutableRows[rowIndex] = row.copy(cells = row.cells + (columnId to value))
        }
    return copy(rows = nextRows)
}

fun DataTableState.withAddedRow(cells: Map<String, String>): DataTableState {
    val nextId = (rows.maxOfOrNull { row -> row.id } ?: 0) + 1
    val nextRow = DataTableRow(id = nextId, cells = cells)
    val nextRows = rows + nextRow
    return copy(rows = nextRows)
}

fun DataTableState.withDeletedRow(index: Int): DataTableState {
    if (rows.size <= minRows) return this
    if (index !in rows.indices) return this
    return copy(rows = rows.filterIndexed { rowIndex, _ -> rowIndex != index })
}

fun formatEditorFloat(value: Float): String =
    value
        .toString()
        .removeSuffix(".0")
