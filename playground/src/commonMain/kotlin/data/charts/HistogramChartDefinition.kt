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
import data.invalidNumericResult
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
import domain.ValidationResult
import domain.deriveFunctionName
import domain.formatEditorFloat
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
        if (dataTable.rows.size < 2) {
            return ValidationResult(
                sanitizedTable = null,
                data = null,
                message = "Histogram chart needs at least 2 rows.",
            )
        }

        val parsed =
            parseEditorTable(
                dataTable = dataTable,
                labelPrefix = "Bin",
                clampToPositive = false,
            ) ?: return invalidNumericResult(dataTable)

        val valueColumn = parsed.numericColumns.firstOrNull() ?: return invalidNumericResult(dataTable)
        val values = parsed.valuesByColumn.getValue(valueColumn.id)
        val negativeRowIds =
            values
                .mapIndexedNotNull { index, value ->
                    if (value < 0f) dataTable.rows.getOrNull(index)?.id else null
                }.toSet()

        if (negativeRowIds.isNotEmpty()) {
            return ValidationResult(
                sanitizedTable = null,
                data = null,
                message = "Histogram values must be non-negative.",
                invalidRowIds = negativeRowIds,
            )
        }

        return ValidationResult(
            sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
            data = ChartData.SingleSeries(values = values, labels = parsed.labels),
            message = "Applied ${parsed.labels.size} rows.",
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
                    val data = it.data as ChartData.SingleSeries
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

    private fun codegenStyleProperties(session: ChartSession): StylePropertiesSnapshot =
        histogramStylePropertiesSnapshot(
            session.styleState as BarStyleState,
            (session.data as ChartData.SingleSeries).values.size,
        )

    override fun generate(session: ChartSession): String {
        val styleProperties = codegenStyleProperties(session)
        val data = session.data as ChartData.SingleSeries
        val points =
            data.values.mapIndexed { index, value ->
                BarPointInput(
                    label = data.labels?.getOrNull(index) ?: "Bin ${index + 1}",
                    valueText = formatEditorFloat(value.coerceAtLeast(0f)),
                )
            }

        return generator
            .generate(
                HistogramCodegenConfig(
                    points = points,
                    title = session.title,
                    styleProperties = styleProperties,
                    codegenMode = session.codegenMode,
                    functionName = deriveFunctionName(session.title, type),
                ),
            ).code
    }
}
