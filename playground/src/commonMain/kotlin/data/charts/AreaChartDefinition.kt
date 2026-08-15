package data.charts

import codegen.AreaCodegenConfig
import codegen.MultiSeriesCodegenInput
import codegen.StylePropertiesSnapshot
import codegen.area.AreaChartCodeGenerator
import codegen.area.areaStylePropertiesSnapshot
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
import domain.AREA_CHART_TITLE
import domain.AreaStyleDefaults
import domain.AreaStyleState
import domain.ChartData
import domain.ChartDefinition
import domain.ChartSession
import domain.ChartStyleState
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.RowId
import domain.SettingDescriptor
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import domain.formatEditorFloat
import kotlin.math.max
import kotlin.random.Random

internal object AreaChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = AreaChartCodeGenerator()

    override val type: ChartType = ChartType.AREA
    override val displayName: String = type.displayName
    override val defaultTitle: String = AREA_CHART_TITLE

    override fun defaultData(): ChartData =
        SampleDataSources.stackedArea
            .initialStackedAreaSample()
            .dataSet
            .toMultiSeries()

    override fun defaultStyleState(): ChartStyleState = AreaStyleState()

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
                chartName = "Area chart",
                minRows = 2,
                requireNonNegative = true,
            )
        if (issues.hasErrors()) return invalidValidationResult(issues)
        val parsed =
            parseEditorTable(
                dataTable = dataTable,
                labelPrefix = "Point",
                clampToPositive = true,
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
            valueProvider = { Random.nextFloat().let { 60f + it * 740f } },
        )

    override fun settingsSchema(session: ChartSession): List<SettingDescriptor> =
        listOf(
            SettingDescriptor.Section("Area"),
            SettingDescriptor.Slider(
                id = "fillAlpha",
                label = "Fill Transparency",
                defaultValue = AreaStyleDefaults.fillAlpha,
                min = 0f,
                max = 1f,
                steps = 20,
                read = { style -> (style as AreaStyleState).fillAlpha },
                write = { style, value -> (style as AreaStyleState).copy(fillAlpha = value) },
            ),
            SettingDescriptor.Toggle(
                id = "lineVisible",
                label = "Show Lines",
                defaultValue = AreaStyleDefaults.lineVisible,
                read = { style -> (style as AreaStyleState).lineVisible },
                write = { style, value -> (style as AreaStyleState).copy(lineVisible = value) },
            ),
            SettingDescriptor.Slider(
                id = "lineWidth",
                label = "Line Width",
                defaultValue = AreaStyleDefaults.lineWidth,
                min = 0f,
                max = 8f,
                steps = 16,
                read = { style -> (style as AreaStyleState).lineWidth },
                write = { style, value -> (style as AreaStyleState).copy(lineWidth = value) },
            ),
            SettingDescriptor.Toggle(
                id = "bezier",
                label = "Use Bezier Curves",
                defaultValue = AreaStyleDefaults.bezier,
                read = { style -> (style as AreaStyleState).bezier },
                write = { style, value -> (style as AreaStyleState).copy(bezier = value) },
            ),
            SettingDescriptor.Toggle(
                id = "zoomControlsVisible",
                label = "Show Zoom Controls",
                defaultValue = AreaStyleDefaults.zoomControlsVisible,
                read = { style -> (style as AreaStyleState).zoomControlsVisible },
                write = { style, value -> (style as AreaStyleState).copy(zoomControlsVisible = value) },
            ),
            SettingDescriptor.ColorPalette(
                id = "areaColors",
                title = "Area Colors",
                itemCount = {
                    val data = it.validatedSpec.data as ChartData.MultiSeries
                    data.series.size
                },
                read = { style -> (style as AreaStyleState).areaColors },
                write = { style, value -> (style as AreaStyleState).copy(areaColors = value) },
            ),
            SettingDescriptor.ColorPalette(
                id = "lineColors",
                title = "Line Colors",
                itemCount = {
                    val data = it.validatedSpec.data as ChartData.MultiSeries
                    data.series.size
                },
                read = { style -> (style as AreaStyleState).lineColors },
                write = { style, value -> (style as AreaStyleState).copy(lineColors = value) },
            ),
        )

    private fun codegenStyleProperties(spec: ValidatedChartSpec): StylePropertiesSnapshot =
        areaStylePropertiesSnapshot(
            spec.styleState as AreaStyleState,
            (spec.data as ChartData.MultiSeries).series.size,
        )

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.MultiSeries
        return generator
            .generate(
                AreaCodegenConfig(
                    series =
                        data.series.map { series ->
                            MultiSeriesCodegenInput(label = series.name, values = series.values)
                        },
                    categories = data.xLabels.orEmpty(),
                    title = spec.title,
                    styleProperties = styleProperties,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
