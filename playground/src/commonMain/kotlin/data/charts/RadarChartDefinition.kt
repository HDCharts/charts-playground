package data.charts

import codegen.MultiSeriesCodegenInput
import codegen.RadarCodegenConfig
import codegen.StylePropertiesSnapshot
import codegen.radar.RadarChartCodeGenerator
import codegen.radar.radarStylePropertiesSnapshot
import data.ChartCodegenAdapter
import data.LABEL_COLUMN_ID
import data.SampleDataSources
import data.defaultRowCells
import data.editorTableIssues
import data.hasErrors
import data.invalidValidationResult
import data.parseEditorTable
import data.randomizeEditorValues
import data.toRadarSeries
import domain.ChartData
import domain.ChartDefinition
import domain.ChartSession
import domain.ChartStyleState
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.RADAR_CHART_TITLE
import domain.RadarStyleDefaults
import domain.RadarStyleState
import domain.RowId
import domain.SettingDescriptor
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import domain.formatEditorFloat
import kotlin.random.Random

internal object RadarChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = RadarChartCodeGenerator()

    override val type: ChartType = ChartType.RADAR
    override val displayName: String = type.displayName
    override val defaultTitle: String = RADAR_CHART_TITLE

    override fun defaultData(): ChartData =
        SampleDataSources.radar
            .initialRadarSample()
            .customDataSet
            .toRadarSeries()

    override fun defaultStyleState(): ChartStyleState = RadarStyleState()

    override fun createDataTable(chartData: ChartData): DataTableState {
        val data = chartData as? ChartData.RadarSeries ?: defaultData() as ChartData.RadarSeries
        val columns =
            buildList {
                add(DataTableColumn(id = LABEL_COLUMN_ID, label = "Axis", numeric = false, weight = 1.4f))
                data.entries.forEachIndexed { index, entry ->
                    add(
                        DataTableColumn(
                            id = "entry_$index",
                            label = entry.name,
                            numeric = true,
                            weight = 1f,
                            defaultValue = "0",
                        ),
                    )
                }
            }

        val rowCount = data.axes.size
        val rows =
            (0 until rowCount).map { rowIndex ->
                val cells = mutableMapOf<String, String>()
                cells[LABEL_COLUMN_ID] = data.axes[rowIndex]
                data.entries.forEachIndexed { entryIndex, entry ->
                    cells["entry_$entryIndex"] = formatEditorFloat(entry.values.getOrElse(rowIndex) { 0f })
                }
                DataTableRow(id = RowId(rowIndex + 1), cells = cells)
            }

        return DataTableState(
            columns = columns,
            rows = rows,
            minRows = 3,
        )
    }

    override fun validate(dataTable: DataTableState): ValidationResult {
        val issues =
            editorTableIssues(
                dataTable = dataTable,
                chartName = "Radar chart",
                minRows = 3,
                requireNonNegative = true,
            )
        if (issues.hasErrors()) return invalidValidationResult(issues)
        val parsed =
            parseEditorTable(
                dataTable = dataTable,
                labelPrefix = "Axis",
                clampToPositive = true,
            ) ?: return invalidValidationResult(issues)

        val entries =
            parsed.numericColumns.map { column ->
                ChartData.RadarSeries.RadarEntry(
                    name = column.label,
                    values = parsed.valuesByColumn.getValue(column.id),
                )
            }

        return ValidationResult(
            sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
            data = ChartData.RadarSeries(entries = entries, axes = parsed.labels),
            issues = issues,
            appliedRowCount = parsed.labels.size,
        )
    }

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Axis")

    override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(
            dataTable = dataTable,
            valueProvider = { Random.nextFloat().let { 35f + it * 65f } },
        )

    override fun settingsSchema(session: ChartSession): List<SettingDescriptor> =
        listOf(
            SettingDescriptor.Section("Radar"),
            SettingDescriptor.Slider(
                id = "lineWidth",
                label = "Line Width",
                defaultValue = RadarStyleDefaults.lineWidth,
                min = 0f,
                max = 8f,
                steps = 16,
                read = { style -> (style as RadarStyleState).lineWidth },
                write = { style, value -> (style as RadarStyleState).copy(lineWidth = value) },
            ),
            SettingDescriptor.Slider(
                id = "pointSize",
                label = "Point Size",
                defaultValue = RadarStyleDefaults.pointSize,
                min = 0f,
                max = 12f,
                steps = 12,
                read = { style -> (style as RadarStyleState).pointSize },
                write = { style, value -> (style as RadarStyleState).copy(pointSize = value) },
            ),
            SettingDescriptor.Slider(
                id = "fillAlpha",
                label = "Fill Transparency",
                defaultValue = RadarStyleDefaults.fillAlpha,
                min = 0f,
                max = 1f,
                steps = 20,
                read = { style -> (style as RadarStyleState).fillAlpha },
                write = { style, value -> (style as RadarStyleState).copy(fillAlpha = value) },
            ),
            SettingDescriptor.Divider,
            SettingDescriptor.Section("Visibility"),
            SettingDescriptor.Toggle(
                id = "fillVisible",
                label = "Show Fill",
                defaultValue = RadarStyleDefaults.fillVisible,
                read = { style -> (style as RadarStyleState).fillVisible },
                write = { style, value -> (style as RadarStyleState).copy(fillVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "pointVisible",
                label = "Show Points",
                defaultValue = RadarStyleDefaults.pointVisible,
                read = { style -> (style as RadarStyleState).pointVisible },
                write = { style, value -> (style as RadarStyleState).copy(pointVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "gridVisible",
                label = "Show Grid",
                defaultValue = RadarStyleDefaults.gridVisible,
                read = { style -> (style as RadarStyleState).gridVisible },
                write = { style, value -> (style as RadarStyleState).copy(gridVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "categoryLegendVisible",
                label = "Show Category Legend",
                defaultValue = RadarStyleDefaults.categoryLegendVisible,
                read = { style -> (style as RadarStyleState).categoryLegendVisible },
                write = { style, value -> (style as RadarStyleState).copy(categoryLegendVisible = value) },
            ),
            SettingDescriptor.ColorPalette(
                id = "lineColors",
                title = "Line Colors",
                itemCount = {
                    val data = it.validatedSpec.data as ChartData.RadarSeries
                    data.entries.size
                },
                read = { style -> (style as RadarStyleState).lineColors },
                write = { style, value -> (style as RadarStyleState).copy(lineColors = value) },
            ),
        )

    private fun codegenStyleProperties(spec: ValidatedChartSpec): StylePropertiesSnapshot =
        radarStylePropertiesSnapshot(
            spec.styleState as RadarStyleState,
            (spec.data as ChartData.RadarSeries).entries.size,
        )

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.RadarSeries
        return generator
            .generate(
                RadarCodegenConfig(
                    series =
                        data.entries.map { entry ->
                            MultiSeriesCodegenInput(label = entry.name, values = entry.values)
                        },
                    categories = data.axes,
                    title = spec.title,
                    styleProperties = styleProperties,
                    codegenMode = spec.codegenMode,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
