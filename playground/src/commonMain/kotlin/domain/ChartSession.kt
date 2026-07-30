package domain

data class ChartSession(
    val chartType: ChartType,
    val title: String,
    val dataTable: DataTableState,
    val data: ChartData,
    val styleState: ChartStyleState,
    val validationMessage: String?,
    val invalidRowIds: Set<Int>,
    val codegenMode: CodegenMode,
    val settings: List<SettingDescriptor>,
    val generatedCode: String,
)
