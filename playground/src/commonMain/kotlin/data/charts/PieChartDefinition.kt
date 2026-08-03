package data.charts

import codegen.PieCodegenConfig
import codegen.PieSliceInput
import codegen.StylePropertiesSnapshot
import codegen.pie.PieChartCodeGenerator
import codegen.pie.pieStylePropertiesSnapshot
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
import domain.PIE_CHART_TITLE
import domain.PieStyleDefaults
import domain.PieStyleState
import domain.SettingDescriptor
import domain.ValidatedChartSpec
import domain.ValidationResult
import domain.deriveFunctionName
import kotlin.random.Random

internal object PieChartDefinition : ChartDefinition, ChartCodegenAdapter {
    private val generator = PieChartCodeGenerator()

    override val type: ChartType = ChartType.PIE
    override val displayName: String = type.displayName
    override val defaultTitle: String = PIE_CHART_TITLE

    override fun defaultData(): ChartData {
        val sample = SampleDataSources.pie.initialPieSample()
        return sample.dataSet.toSingleSeries(labelsOverride = sample.segmentKeys)
    }

    override fun defaultStyleState(): ChartStyleState = PieStyleState()

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
            chartName = "Pie chart",
            minRows = 2,
            labelPrefix = "Slice",
            clampToPositive = true,
        )

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Slice")

    override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(
            dataTable = dataTable,
            valueProvider = { Random.nextFloat().let { 8f + it * 48f } },
        )

    override fun settingsSchema(session: ChartSession): List<SettingDescriptor> {
        val itemCount = (session.validatedSpec.data as ChartData.SingleSeries).values.size
        return listOf(
            SettingDescriptor.Section("Pie Chart"),
            SettingDescriptor.Slider(
                id = "donutPercentage",
                label = "Donut Hole Size",
                defaultValue = PieStyleDefaults.donutPercentage,
                min = 0f,
                max = 70f,
                steps = 20,
                read = { style -> (style as PieStyleState).donutPercentage },
                write = { style, value -> (style as PieStyleState).copy(donutPercentage = value) },
                format = { value -> "${value.toInt()}%" },
            ),
            SettingDescriptor.Slider(
                id = "borderWidth",
                label = "Border Width",
                defaultValue = PieStyleDefaults.borderWidth,
                min = 0f,
                max = 10f,
                steps = 20,
                read = { style -> (style as PieStyleState).borderWidth },
                write = { style, value -> (style as PieStyleState).copy(borderWidth = value) },
            ),
            SettingDescriptor.Slider(
                id = "pieAlpha",
                label = "Slice Transparency",
                defaultValue = PieStyleDefaults.pieAlpha,
                min = 0f,
                max = 1f,
                steps = 20,
                read = { style -> (style as PieStyleState).pieAlpha },
                write = { style, value -> (style as PieStyleState).copy(pieAlpha = value) },
            ),
            SettingDescriptor.Toggle(
                id = "legendVisible",
                label = "Show Legend",
                defaultValue = PieStyleDefaults.legendVisible,
                read = { style -> (style as PieStyleState).legendVisible },
                write = { style, value -> (style as PieStyleState).copy(legendVisible = value) },
            ),
            SettingDescriptor.ColorPalette(
                id = "pieColors",
                title = "Slice Colors",
                itemCount = { itemCount },
                read = { style -> (style as PieStyleState).pieColors },
                write = { style, value -> (style as PieStyleState).copy(pieColors = value) },
            ),
        )
    }

    private fun codegenStyleProperties(spec: ValidatedChartSpec): StylePropertiesSnapshot =
        pieStylePropertiesSnapshot(
            spec.styleState as PieStyleState,
            (spec.data as ChartData.SingleSeries).values.size,
        )

    override fun generate(spec: ValidatedChartSpec): String {
        val styleProperties = codegenStyleProperties(spec)
        val data = spec.data as ChartData.SingleSeries
        val rows =
            data.values.mapIndexed { index, value ->
                PieSliceInput(
                    label = data.labels?.getOrNull(index) ?: "Slice ${index + 1}",
                    value = value,
                )
            }

        return generator
            .generate(
                PieCodegenConfig(
                    rows = rows,
                    title = spec.title,
                    styleProperties = styleProperties,
                    codegenMode = spec.codegenMode,
                    functionName = deriveFunctionName(spec.title, type),
                ),
            ).code
    }
}
