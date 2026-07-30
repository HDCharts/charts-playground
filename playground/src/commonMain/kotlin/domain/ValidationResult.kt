package domain

data class ValidationResult(
    val sanitizedTable: DataTableState?,
    val data: ChartData?,
    val message: String,
    val invalidRowIds: Set<Int> = emptySet(),
)
