package data.charts

import codegen.MultiLineCodegenConfig
import codegen.MultiSeriesCodegenInput
import codegen.StylePropertiesSnapshot
import codegen.multiline.MultiLineChartCodeGenerator
import codegen.multiline.multiLineStylePropertiesSnapshot
import data.ChartCodegenAdapter
import data.LABEL_COLUMN_ID
import data.SampleDataSources
import data.defaultRowCells
import data.editorTableIssues
import data.hasErrors
import data.invalidValidationResult
import data.parseEditorTable
import data.randomizeEditorValues
import data.toMultiSeries
import domain.ChartData
import domain.ChartDefinition
import domain.ChartSession
import domain.ChartStyleState
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.MULTI_LINE_CHART_TITLE
import domain.MultiLineStyleDefaults
import domain.MultiLineStyleState
import domain.RowId
import domain.SettingDescriptor
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import domain.formatEditorFloat
import kotlin.math.max
import kotlin.random.Random

internal object MultiLineChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = MultiLineChartCodeGenerator()

    override val type: ChartType = ChartType.MULTI_LINE
    override val displayName: String = type.displayName
    override val defaultTitle: String = MULTI_LINE_CHART_TITLE

    override fun defaultData(): ChartData =
        SampleDataSources.multiLine
            .initialMultiLineSample()
            .dataSet
            .toMultiSeries()

    override fun defaultStyleState(): ChartStyleState = MultiLineStyleState()

    override fun createDataTable(chartData: ChartData): DataTableState {
        val data = chartData as? ChartData.MultiSeries ?: defaultData() as ChartData.MultiSeries
        val columns =
            buildList {
                add(DataTableColumn(id = LABEL_COLUMN_ID, label = "Category", numeric = false, weight = 1.4f))
                data.series.forEachIndexed { index, series ->
                    add(
                        DataTableColumn(
                            id = "series_$index",
                            label = series.name,
                            numeric = true,
                            weight = 1f,
                            defaultValue = "0",
                        ),
                    )
                }
            }

        val categories = data.xLabels.orEmpty()
        val rowCount = max(categories.size, data.series.maxOfOrNull { series -> series.values.size } ?: 0)
        val rows =
            (0 until rowCount).map { rowIndex ->
                val cells = mutableMapOf<String, String>()
                cells[LABEL_COLUMN_ID] = categories.getOrNull(rowIndex) ?: "Point ${rowIndex + 1}"
                data.series.forEachIndexed { seriesIndex, series ->
                    cells["series_$seriesIndex"] = formatEditorFloat(series.values.getOrElse(rowIndex) { 0f })
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
                chartName = "Multi line chart",
                minRows = 2,
                requireNonNegative = false,
            )
        if (issues.hasErrors()) return invalidValidationResult(issues)
        val parsed =
            parseEditorTable(
                dataTable = dataTable,
                labelPrefix = "Point",
                clampToPositive = false,
            ) ?: return invalidValidationResult(issues)

        val series =
            parsed.numericColumns.map { column ->
                ChartData.MultiSeries.Series(
                    name = column.label,
                    values = parsed.valuesByColumn.getValue(column.id),
                )
            }

        return ValidationResult(
            sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
            data = ChartData.MultiSeries(series = series, xLabels = parsed.labels),
            issues = issues,
            appliedRowCount = parsed.labels.size,
        )
    }

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Point")

    override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(
            dataTable = dataTable,
            valueProvider = { Random.nextFloat().let { 220f + it * 520f } },
        )

    override fun settingsSchema(session: ChartSession): List<SettingDescriptor> =
        listOf(
            SettingDescriptor.Section("Multi Line"),
            SettingDescriptor.Slider(
                id = "lineAlpha",
                label = "Line Transparency",
                defaultValue = MultiLineStyleDefaults.lineAlpha,
                min = 0f,
                max = 1f,
                steps = 20,
                read = { style -> (style as MultiLineStyleState).lineAlpha },
                write = { style, value -> (style as MultiLineStyleState).copy(lineAlpha = value) },
            ),
            SettingDescriptor.Toggle(
                id = "bezier",
                label = "Use Bezier Curves",
                defaultValue = MultiLineStyleDefaults.bezier,
                read = { style -> (style as MultiLineStyleState).bezier },
                write = { style, value -> (style as MultiLineStyleState).copy(bezier = value) },
            ),
            SettingDescriptor.Toggle(
                id = "pointVisible",
                label = "Show Points",
                defaultValue = MultiLineStyleDefaults.pointVisible,
                read = { style -> (style as MultiLineStyleState).pointVisible },
                write = { style, value -> (style as MultiLineStyleState).copy(pointVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "dragPointVisible",
                label = "Show Drag Point",
                defaultValue = MultiLineStyleDefaults.dragPointVisible,
                read = { style -> (style as MultiLineStyleState).dragPointVisible },
                write = { style, value -> (style as MultiLineStyleState).copy(dragPointVisible = value) },
            ),
            SettingDescriptor.ColorPalette(
                id = "lineColors",
                title = "Line Colors",
                itemCount = {
                    val data = it.validatedSpec.data as ChartData.MultiSeries
                    data.series.size
                },
                read = { style -> (style as MultiLineStyleState).lineColors },
                write = { style, value -> (style as MultiLineStyleState).copy(lineColors = value) },
            ),
            SettingDescriptor.Color(
                id = "pointColor",
                label = "Point Color",
                read = { style -> (style as MultiLineStyleState).pointColor },
                write = { style, value -> (style as MultiLineStyleState).copy(pointColor = value) },
            ),
            SettingDescriptor.Color(
                id = "dragPointColor",
                label = "Drag Point Color",
                read = { style -> (style as MultiLineStyleState).dragPointColor },
                write = { style, value -> (style as MultiLineStyleState).copy(dragPointColor = value) },
            ),
        )

    private fun codegenStyleProperties(spec: ValidatedChartSpec): StylePropertiesSnapshot =
        multiLineStylePropertiesSnapshot(
            spec.styleState as MultiLineStyleState,
            (spec.data as ChartData.MultiSeries).series.size,
        )

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.MultiSeries
        return generator
            .generate(
                MultiLineCodegenConfig(
                    series =
                        data.series.map { series ->
                            MultiSeriesCodegenInput(label = series.name, values = series.values)
                        },
                    categories = data.xLabels.orEmpty(),
                    title = spec.title,
                    styleProperties = styleProperties,
                    codegenMode = spec.codegenMode,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
