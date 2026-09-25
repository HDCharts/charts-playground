package domain

interface ChartDefinition {
    val type: ChartType
    val displayName: String
    val defaultTitle: String

    fun defaultData(): ChartData

    fun createDataTable(chartData: ChartData): DataTableState

    fun validate(dataTable: DataTableState): ValidationResult

    fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String>

    fun randomize(dataTable: DataTableState): DataTableState

    /** The chart's editable settings, in display order. */
    val settings: List<SettingDescriptor>

    fun resetSession(): ChartSession {
        val data = defaultData()
        val dataTable = createDataTable(data)
        val draft =
            ChartDraft(
                title = defaultTitle,
                dataTable = dataTable,
                styleState = ChartStyleState(),
            )
        return ChartSession(
            chartType = type,
            draft = draft,
            validatedSpec =
                ValidatedChartSpec(
                    chartType = type,
                    title = draft.title,
                    data = data,
                    styleState = draft.styleState,
                ),
            validation = ChartValidationState.Valid(),
            settings = settings,
            generatedCode = "",
        )
    }
}
