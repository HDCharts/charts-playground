package domain

const val PIE_CHART_TITLE = "Revenue Breakdown"
const val LINE_CHART_TITLE = "Monthly Trend"
const val MULTI_LINE_CHART_TITLE = "Revenue By Channel"
const val BAR_CHART_TITLE = "Weekly Performance"
const val HISTOGRAM_CHART_TITLE = "Request Duration Distribution"
const val STACKED_BAR_CHART_TITLE = "Quarterly Revenue Mix"
const val AREA_CHART_TITLE = "Plan Distribution"
const val RADAR_CHART_TITLE = "Platform Capability"

interface ChartDefinition {
    val type: ChartType
    val displayName: String
    val defaultTitle: String

    fun defaultData(): ChartData

    fun defaultStyleState(): ChartStyleState

    fun createDataTable(chartData: ChartData): DataTableState

    fun validate(dataTable: DataTableState): ValidationResult

    fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String>

    fun randomize(dataTable: DataTableState): DataTableState

    fun settingsSchema(session: ChartSession): List<SettingDescriptor>

    fun resetSession(codegenMode: CodegenMode = CodegenMode.MINIMAL): ChartSession {
        val data = defaultData()
        val dataTable = createDataTable(data)
        return ChartSession(
            chartType = type,
            title = defaultTitle,
            dataTable = dataTable,
            data = data,
            styleState = defaultStyleState(),
            validationMessage = null,
            invalidRowIds = emptySet(),
            codegenMode = codegenMode,
            settings = emptyList(),
            generatedCode = "",
        )
    }
}
