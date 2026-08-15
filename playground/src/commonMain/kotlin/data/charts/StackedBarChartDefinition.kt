package data.charts

import codegen.MultiSeriesCodegenInput
import codegen.StackedBarCodegenConfig
import codegen.StylePropertiesSnapshot
import codegen.stackedbar.StackedBarChartCodeGenerator
import codegen.stackedbar.stackedBarStylePropertiesSnapshot
import data.ChartCodegenAdapter
import data.LABEL_COLUMN_ID
import data.SampleDataSources
import data.defaultRowCells
import data.editorTableIssues
import data.hasErrors
import data.invalidValidationResult
import data.parseEditorTable
import data.randomizeEditorValues
import data.toStackedSeries
import domain.ChartData
import domain.ChartDefinition
import domain.ChartSession
import domain.ChartStyleState
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.RowId
import domain.STACKED_BAR_CHART_TITLE
import domain.SettingDescriptor
import domain.StackedBarStyleDefaults
import domain.StackedBarStyleState
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import domain.formatEditorFloat
import kotlin.random.Random

internal object StackedBarChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = StackedBarChartCodeGenerator()

    override val type: ChartType = ChartType.STACKED_BAR
    override val displayName: String = type.displayName
    override val defaultTitle: String = STACKED_BAR_CHART_TITLE

    override fun defaultData(): ChartData =
        SampleDataSources.stackedBar
            .initialStackedBarSample()
            .dataSet
            .toStackedSeries()

    override fun defaultStyleState(): ChartStyleState = StackedBarStyleState()

    override fun createDataTable(chartData: ChartData): DataTableState {
        val data =
            chartData as? ChartData.StackedSeries ?: defaultData() as ChartData.StackedSeries
        val columns =
            buildList {
                add(DataTableColumn(id = LABEL_COLUMN_ID, label = "Category", numeric = false, weight = 1.4f))
                data.segmentNames.forEachIndexed { index, name ->
                    add(
                        DataTableColumn(
                            id = "segment_$index",
                            label = name,
                            numeric = true,
                            weight = 1f,
                            defaultValue = "0",
                        ),
                    )
                }
            }

        val rowCount = data.bars.size
        val rows =
            (0 until rowCount).map { rowIndex ->
                val bar = data.bars[rowIndex]
                val cells = mutableMapOf<String, String>()
                cells[LABEL_COLUMN_ID] = bar.label
                data.segmentNames.indices.forEach { segmentIndex ->
                    cells["segment_$segmentIndex"] = formatEditorFloat(bar.values.getOrElse(segmentIndex) { 0f })
                }
                DataTableRow(id = RowId(rowIndex + 1), cells = cells)
            }

        return DataTableState(
            columns = columns,
            rows = rows,
            minRows = 2,
        )
    }

    override fun validate(dataTable: DataTableState): ValidationResult {
        val issues =
            editorTableIssues(
                dataTable = dataTable,
                chartName = "Stacked bar chart",
                minRows = 2,
                requireNonNegative = true,
            )
        if (issues.hasErrors()) return invalidValidationResult(issues)
        val parsed =
            parseEditorTable(
                dataTable = dataTable,
                labelPrefix = "Bar",
                clampToPositive = true,
            ) ?: return invalidValidationResult(issues)

        val segmentNames = parsed.numericColumns.map { column -> column.label }
        val bars =
            parsed.labels.mapIndexed { rowIndex, label ->
                val values = parsed.numericColumns.map { column -> parsed.valuesByColumn.getValue(column.id)[rowIndex] }
                ChartData.StackedSeries.StackedBar(
                    label = label,
                    values = values,
                )
            }

        return ValidationResult(
            sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
            data =
                ChartData.StackedSeries(
                    segmentNames = segmentNames,
                    bars = bars,
                    labels = parsed.labels,
                ),
            issues = issues,
            appliedRowCount = parsed.labels.size,
        )
    }

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Bar")

    override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(
            dataTable = dataTable,
            valueProvider = { Random.nextFloat().let { 30f + it * 180f } },
        )

    override fun settingsSchema(session: ChartSession): List<SettingDescriptor> =
        listOf(
            SettingDescriptor.Section("Stacked Bar"),
            SettingDescriptor.Slider(
                id = "barAlpha",
                label = "Bar Transparency",
                defaultValue = StackedBarStyleDefaults.barAlpha,
                min = 0f,
                max = 1f,
                steps = 20,
                read = { style -> (style as StackedBarStyleState).barAlpha },
                write = { style, value -> (style as StackedBarStyleState).copy(barAlpha = value) },
            ),
            SettingDescriptor.ColorPalette(
                id = "barColors",
                title = "Segment Colors",
                itemCount = {
                    val data = it.validatedSpec.data as ChartData.StackedSeries
                    data.segmentNames.size
                },
                read = { style -> (style as StackedBarStyleState).barColors },
                write = { style, value -> (style as StackedBarStyleState).copy(barColors = value) },
            ),
            SettingDescriptor.Divider,
            SettingDescriptor.Section("Interaction"),
            SettingDescriptor.Toggle(
                id = "selectionLineVisible",
                label = "Show Selection Line",
                defaultValue = StackedBarStyleDefaults.selectionLineVisible,
                read = { style -> (style as StackedBarStyleState).selectionLineVisible },
                write = { style, value -> (style as StackedBarStyleState).copy(selectionLineVisible = value) },
            ),
            SettingDescriptor.Slider(
                id = "selectionLineWidth",
                label = "Selection Line Width",
                defaultValue = StackedBarStyleDefaults.selectionLineWidth,
                min = 0f,
                max = 4f,
                steps = 16,
                read = { style -> (style as StackedBarStyleState).selectionLineWidth },
                write = { style, value -> (style as StackedBarStyleState).copy(selectionLineWidth = value) },
            ),
            SettingDescriptor.Toggle(
                id = "zoomControlsVisible",
                label = "Show Zoom Controls",
                defaultValue = StackedBarStyleDefaults.zoomControlsVisible,
                read = { style -> (style as StackedBarStyleState).zoomControlsVisible },
                write = { style, value -> (style as StackedBarStyleState).copy(zoomControlsVisible = value) },
            ),
        )

    private fun codegenStyleProperties(spec: ValidatedChartSpec): StylePropertiesSnapshot =
        stackedBarStylePropertiesSnapshot(
            spec.styleState as StackedBarStyleState,
            (spec.data as ChartData.StackedSeries).segmentNames.size,
        )

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.StackedSeries
        val series =
            data.segmentNames.mapIndexed { segmentIndex, name ->
                MultiSeriesCodegenInput(
                    label = name,
                    values = data.bars.map { bar -> bar.values.getOrElse(segmentIndex) { 0f } },
                )
            }
        val categories = data.bars.map { bar -> bar.label }

        return generator
            .generate(
                StackedBarCodegenConfig(
                    series = series,
                    categories = categories,
                    title = spec.title,
                    styleProperties = styleProperties,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
