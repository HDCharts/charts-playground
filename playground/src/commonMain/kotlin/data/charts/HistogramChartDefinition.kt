package data.charts

import codegen.BarPointInput
import codegen.HistogramCodegenConfig
import codegen.StylePropertiesSnapshot
import codegen.histogram.HistogramChartCodeGenerator
import codegen.histogram.histogramStylePropertiesSnapshot
import data.ChartCodegenAdapter
import data.SampleDataSources
import data.createSingleSeriesTable
import data.defaultRowCells
import data.editorTableIssues
import data.hasErrors
import data.invalidValidationResult
import data.parseEditorTable
import data.randomizeEditorValues
import data.toSingleSeries
import domain.BarStyleDefaults
import domain.BarStyleState
import domain.ChartData
import domain.ChartDefinition
import domain.ChartSession
import domain.ChartStyleState
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableState
import domain.HISTOGRAM_CHART_TITLE
import domain.SettingDescriptor
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import kotlin.random.Random

internal object HistogramChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = HistogramChartCodeGenerator()

    override val type: ChartType = ChartType.HISTOGRAM
    override val displayName: String = type.displayName
    override val defaultTitle: String = HISTOGRAM_CHART_TITLE

    override fun defaultData(): ChartData = SampleDataSources.histogram.initialHistogramDataSet().toSingleSeries()

    override fun defaultStyleState(): ChartStyleState = BarStyleState()

    override fun createDataTable(chartData: ChartData): DataTableState {
        val data = chartData as? ChartData.SingleSeries ?: defaultData() as ChartData.SingleSeries
        return createSingleSeriesTable(
            model = data,
            minRows = 2,
            labelHeader = "Bin",
        )
    }

    override fun validate(dataTable: DataTableState): ValidationResult {
        val issues =
            editorTableIssues(
                dataTable = dataTable,
                chartName = "Histogram chart",
                minRows = 2,
                requireNonNegative = true,
            )
        if (issues.hasErrors()) return invalidValidationResult(issues)

        val parsed =
            parseEditorTable(
                dataTable = dataTable,
                labelPrefix = "Bin",
                clampToPositive = false,
            ) ?: return invalidValidationResult(issues)

        val valueColumn = parsed.numericColumns.firstOrNull() ?: return invalidValidationResult(issues)
        val values = parsed.valuesByColumn.getValue(valueColumn.id)

        return ValidationResult(
            sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
            data = ChartData.SingleSeries(values = values, labels = parsed.labels),
            issues = issues,
            appliedRowCount = parsed.labels.size,
        )
    }

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Bin")

    override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(
            dataTable = dataTable,
            valueProvider = { Random.nextFloat() * 80f },
        )

    override fun settingsSchema(session: ChartSession): List<SettingDescriptor> =
        listOf(
            SettingDescriptor.Section("Bars"),
            SettingDescriptor.Slider(
                id = "barAlpha",
                label = "Bar Transparency",
                defaultValue = BarStyleDefaults.barAlpha,
                min = 0f,
                max = 1f,
                steps = 20,
                read = { style -> (style as BarStyleState).barAlpha },
                write = { style, value -> (style as BarStyleState).copy(barAlpha = value) },
            ),
            SettingDescriptor.Color(
                id = "barColor",
                label = "Bar Color",
                read = { style -> (style as BarStyleState).barColor },
                write = { style, value -> (style as BarStyleState).copy(barColor = value) },
            ),
            SettingDescriptor.ColorPalette(
                id = "barColors",
                title = "Bar Colors",
                itemCount = {
                    val data = it.validatedSpec.data as ChartData.SingleSeries
                    data.values.size
                },
                read = { style -> (style as BarStyleState).barColors },
                write = { style, value -> (style as BarStyleState).copy(barColors = value) },
            ),
            SettingDescriptor.Divider,
            SettingDescriptor.Section("Visibility"),
            SettingDescriptor.Toggle(
                id = "gridVisible",
                label = "Show Grid",
                defaultValue = BarStyleDefaults.gridVisible,
                read = { style -> (style as BarStyleState).gridVisible },
                write = { style, value -> (style as BarStyleState).copy(gridVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "axisVisible",
                label = "Show Axes",
                defaultValue = BarStyleDefaults.axisVisible,
                read = { style -> (style as BarStyleState).axisVisible },
                write = { style, value -> (style as BarStyleState).copy(axisVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "selectionLineVisible",
                label = "Show Selection Line",
                defaultValue = BarStyleDefaults.selectionLineVisible,
                read = { style -> (style as BarStyleState).selectionLineVisible },
                write = { style, value -> (style as BarStyleState).copy(selectionLineVisible = value) },
            ),
            SettingDescriptor.Slider(
                id = "selectionLineWidth",
                label = "Selection Line Width",
                defaultValue = BarStyleDefaults.selectionLineWidth,
                min = 0f,
                max = 4f,
                steps = 16,
                read = { style -> (style as BarStyleState).selectionLineWidth },
                write = { style, value -> (style as BarStyleState).copy(selectionLineWidth = value) },
            ),
            SettingDescriptor.Toggle(
                id = "zoomControlsVisible",
                label = "Show Zoom Controls",
                defaultValue = BarStyleDefaults.zoomControlsVisible,
                read = { style -> (style as BarStyleState).zoomControlsVisible },
                write = { style, value -> (style as BarStyleState).copy(zoomControlsVisible = value) },
            ),
        )

    private fun codegenStyleProperties(spec: ValidatedChartSpec): StylePropertiesSnapshot =
        histogramStylePropertiesSnapshot(
            spec.styleState as BarStyleState,
            (spec.data as ChartData.SingleSeries).values.size,
        )

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.SingleSeries
        val points =
            data.values.mapIndexed { index, value ->
                BarPointInput(
                    label = data.labels?.getOrNull(index) ?: "Bin ${index + 1}",
                    value = value,
                )
            }

        return generator
            .generate(
                HistogramCodegenConfig(
                    points = points,
                    title = spec.title,
                    styleProperties = styleProperties,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
