package data.charts

import codegen.BarCodegenConfig
import codegen.PieSliceInput
import codegen.StylePropertiesSnapshot
import codegen.bar.BarChartCodeGenerator
import codegen.bar.barStylePropertiesSnapshot
import data.ChartCodegenAdapter
import data.SampleDataSources
import data.createSingleSeriesTable
import data.defaultRowCells
import data.randomizeEditorValues
import data.toSingleSeries
import data.validateSingleSeries
import domain.BAR_CHART_TITLE
import domain.BarStyleDefaults
import domain.BarStyleState
import domain.ChartData
import domain.ChartDefinition
import domain.ChartSession
import domain.ChartStyleState
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableState
import domain.SettingDescriptor
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import kotlin.random.Random

internal object BarChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = BarChartCodeGenerator()

    override val type: ChartType = ChartType.BAR
    override val displayName: String = type.displayName
    override val defaultTitle: String = BAR_CHART_TITLE

    override fun defaultData(): ChartData = SampleDataSources.bar.initialBarDataSet().toSingleSeries()

    override fun defaultStyleState(): ChartStyleState = BarStyleState()

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
            chartName = "Bar chart",
            minRows = 2,
            labelPrefix = "Bar",
            clampToPositive = false,
        )

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Bar")

    override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(
            dataTable = dataTable,
            valueProvider = { Random.nextFloat().let { -25f + it * 85f } },
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
        barStylePropertiesSnapshot(
            spec.styleState as BarStyleState,
            (spec.data as ChartData.SingleSeries).values.size,
        )

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.SingleSeries
        val points =
            data.values.mapIndexed { index, value ->
                PieSliceInput(
                    label = data.labels?.getOrNull(index) ?: "Bar ${index + 1}",
                    value = value,
                )
            }

        return generator
            .generate(
                BarCodegenConfig(
                    points = points,
                    title = spec.title,
                    styleProperties = styleProperties,
                    codegenMode = spec.codegenMode,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
