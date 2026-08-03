package data.charts

import codegen.LineCodegenConfig
import codegen.PieSliceInput
import codegen.StylePropertiesSnapshot
import codegen.line.LineChartCodeGenerator
import codegen.line.lineStylePropertiesSnapshot
import data.ChartCodegenAdapter
import data.SampleDataSources
import data.createSingleSeriesTable
import data.defaultRowCells
import data.randomizeEditorValues
import data.toSingleSeries
import data.validateSingleSeries
import domain.ChartData
import domain.ChartDefinition
import domain.ChartSession
import domain.ChartStyleState
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableState
import domain.DropdownOption
import domain.LINE_CHART_TITLE
import domain.LineStyleDefaults
import domain.LineStyleState
import domain.SettingDescriptor
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import kotlin.random.Random

internal object LineChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = LineChartCodeGenerator()

    override val type: ChartType = ChartType.LINE
    override val displayName: String = type.displayName
    override val defaultTitle: String = LINE_CHART_TITLE

    override fun defaultData(): ChartData = SampleDataSources.line.initialLineDataSet().toSingleSeries()

    override fun defaultStyleState(): ChartStyleState = LineStyleState()

    override fun createDataTable(chartData: ChartData): DataTableState {
        val data = chartData as? ChartData.SingleSeries ?: defaultData() as ChartData.SingleSeries
        return createSingleSeriesTable(
            model = data,
            minRows = 2,
            labelHeader = "Label",
        )
    }

    override fun validate(dataTable: DataTableState): ValidationResult =
        validateSingleSeries(
            dataTable = dataTable,
            chartName = "Line chart",
            minRows = 2,
            labelPrefix = "Point",
            clampToPositive = false,
        )

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Point")

    override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(
            dataTable = dataTable,
            valueProvider = { Random.nextFloat().let { 8f + it * 36f } },
        )

    override fun settingsSchema(session: ChartSession): List<SettingDescriptor> =
        listOf(
            SettingDescriptor.Section("Series"),
            SettingDescriptor.Slider(
                id = "lineAlpha",
                label = "Line Transparency",
                defaultValue = LineStyleDefaults.lineAlpha,
                min = 0f,
                max = 1f,
                steps = 20,
                read = { style -> (style as LineStyleState).lineAlpha },
                write = { style, value -> (style as LineStyleState).copy(lineAlpha = value) },
            ),
            SettingDescriptor.Dropdown(
                id = "curveMode",
                label = "Curve Mode",
                options =
                    listOf(
                        DropdownOption(label = "Bezier", value = "bezier"),
                        DropdownOption(label = "Linear", value = "linear"),
                    ),
                defaultValue = if (LineStyleDefaults.bezier) "bezier" else "linear",
                read = { style -> if ((style as LineStyleState).bezier == false) "linear" else "bezier" },
                write = { style, value -> (style as LineStyleState).copy(bezier = value != "linear") },
            ),
            SettingDescriptor.Toggle(
                id = "pointVisible",
                label = "Show Points",
                defaultValue = LineStyleDefaults.pointVisible,
                read = { style -> (style as LineStyleState).pointVisible },
                write = { style, value -> (style as LineStyleState).copy(pointVisible = value) },
            ),
            SettingDescriptor.Slider(
                id = "pointSize",
                label = "Point Size",
                defaultValue = LineStyleDefaults.pointSize,
                min = 2f,
                max = 20f,
                steps = 18,
                read = { style -> (style as LineStyleState).pointSize },
                write = { style, value -> (style as LineStyleState).copy(pointSize = value) },
            ),
            SettingDescriptor.Divider,
            SettingDescriptor.Section("Colors"),
            SettingDescriptor.Color(
                id = "lineColor",
                label = "Line Color",
                read = { style -> (style as LineStyleState).lineColor },
                write = { style, value -> (style as LineStyleState).copy(lineColor = value) },
            ),
            SettingDescriptor.Color(
                id = "pointColor",
                label = "Point Color",
                read = { style -> (style as LineStyleState).pointColor },
                write = { style, value -> (style as LineStyleState).copy(pointColor = value) },
            ),
            SettingDescriptor.Color(
                id = "dragPointColor",
                label = "Drag Point Color",
                read = { style -> (style as LineStyleState).dragPointColor },
                write = { style, value -> (style as LineStyleState).copy(dragPointColor = value) },
            ),
            SettingDescriptor.Divider,
            SettingDescriptor.Section("Interaction"),
            SettingDescriptor.Toggle(
                id = "dragPointVisible",
                label = "Show Drag Point",
                defaultValue = LineStyleDefaults.dragPointVisible,
                read = { style -> (style as LineStyleState).dragPointVisible },
                write = { style, value -> (style as LineStyleState).copy(dragPointVisible = value) },
            ),
            SettingDescriptor.Slider(
                id = "dragPointSize",
                label = "Drag Point Size",
                defaultValue = LineStyleDefaults.dragPointSize,
                min = 2f,
                max = 20f,
                steps = 18,
                read = { style -> (style as LineStyleState).dragPointSize },
                write = { style, value -> (style as LineStyleState).copy(dragPointSize = value) },
            ),
            SettingDescriptor.Slider(
                id = "dragActivePointSize",
                label = "Active Drag Point Size",
                defaultValue = LineStyleDefaults.dragActivePointSize,
                min = 2f,
                max = 24f,
                steps = 22,
                read = { style -> (style as LineStyleState).dragActivePointSize },
                write = { style, value -> (style as LineStyleState).copy(dragActivePointSize = value) },
            ),
            SettingDescriptor.Toggle(
                id = "zoomControlsVisible",
                label = "Show Zoom Controls",
                defaultValue = LineStyleDefaults.zoomControlsVisible,
                read = { style -> (style as LineStyleState).zoomControlsVisible },
                write = { style, value -> (style as LineStyleState).copy(zoomControlsVisible = value) },
            ),
            SettingDescriptor.Divider,
            SettingDescriptor.Section("Axes"),
            SettingDescriptor.Toggle(
                id = "axisVisible",
                label = "Show Axes",
                defaultValue = LineStyleDefaults.axisVisible,
                read = { style -> (style as LineStyleState).axisVisible },
                write = { style, value -> (style as LineStyleState).copy(axisVisible = value) },
            ),
            SettingDescriptor.Slider(
                id = "axisLineWidth",
                label = "Axis Line Width",
                defaultValue = LineStyleDefaults.axisLineWidth,
                min = 0f,
                max = 4f,
                steps = 16,
                read = { style -> (style as LineStyleState).axisLineWidth },
                write = { style, value -> (style as LineStyleState).copy(axisLineWidth = value) },
            ),
            SettingDescriptor.Toggle(
                id = "xAxisLabelsVisible",
                label = "Show X Labels",
                defaultValue = LineStyleDefaults.xAxisLabelsVisible,
                read = { style -> (style as LineStyleState).xAxisLabelsVisible },
                write = { style, value -> (style as LineStyleState).copy(xAxisLabelsVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "yAxisLabelsVisible",
                label = "Show Y Labels",
                defaultValue = LineStyleDefaults.yAxisLabelsVisible,
                read = { style -> (style as LineStyleState).yAxisLabelsVisible },
                write = { style, value -> (style as LineStyleState).copy(yAxisLabelsVisible = value) },
            ),
        )

    private fun codegenStyleProperties(spec: ValidatedChartSpec): StylePropertiesSnapshot =
        lineStylePropertiesSnapshot(spec.styleState as LineStyleState)

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.SingleSeries
        val points =
            data.values.mapIndexed { index, value ->
                PieSliceInput(
                    label = data.labels?.getOrNull(index) ?: "Point ${index + 1}",
                    value = value,
                )
            }

        return generator
            .generate(
                LineCodegenConfig(
                    points = points,
                    title = spec.title,
                    styleProperties = styleProperties,
                    codegenMode = spec.codegenMode,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
