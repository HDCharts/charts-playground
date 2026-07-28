package model.definitions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import codegen.histogram.HistogramChartCodeGenerator
import codegen.histogram.histogramStylePropertiesSnapshot
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import model.BarPointInput
import model.BarStyleState
import model.ChartType
import model.DataEditorColumn
import model.DataEditorState
import model.GeneratedSnippet
import model.HISTOGRAM_CHART_TITLE
import model.HistogramCodegenConfig
import model.PlaygroundChartDefinition
import model.PlaygroundChartSession
import model.PlaygroundDataModel
import model.PlaygroundStyleState
import model.PlaygroundValidationResult
import model.SettingDescriptor
import model.deriveFunctionName
import model.formatEditorFloat
import kotlin.random.Random

internal object HistogramChartDefinition : PlaygroundChartDefinition {
    private val generator = HistogramChartCodeGenerator()

    override val type: ChartType = ChartType.HISTOGRAM
    override val displayName: String = type.displayName
    override val defaultTitle: String = HISTOGRAM_CHART_TITLE

    override fun defaultDataModel(): PlaygroundDataModel =
        PlaygroundSampleUseCases.histogram.initialHistogramDataSet().toSimpleSeries()

    override fun defaultStyleState(): PlaygroundStyleState = BarStyleState()

    override fun createEditorState(model: PlaygroundDataModel): DataEditorState {
        val data = model as? PlaygroundDataModel.SimpleSeries ?: defaultDataModel() as PlaygroundDataModel.SimpleSeries
        return createSimpleSeriesEditor(
            model = data,
            minRows = 2,
            labelHeader = "Bin",
        )
    }

    override fun validate(editorState: DataEditorState): PlaygroundValidationResult {
        if (editorState.rows.size < 2) {
            return PlaygroundValidationResult(
                sanitizedEditor = null,
                dataModel = null,
                message = "Histogram chart needs at least 2 rows.",
            )
        }

        val parsed =
            parseEditorTable(
                editorState = editorState,
                labelPrefix = "Bin",
                clampToPositive = false,
            ) ?: return invalidNumericResult(editorState)

        val valueColumn = parsed.numericColumns.firstOrNull() ?: return invalidNumericResult(editorState)
        val values = parsed.valuesByColumn.getValue(valueColumn.id)
        val negativeRowIds =
            values
                .mapIndexedNotNull { index, value ->
                    if (value < 0f) editorState.rows.getOrNull(index)?.id else null
                }.toSet()

        if (negativeRowIds.isNotEmpty()) {
            return PlaygroundValidationResult(
                sanitizedEditor = null,
                dataModel = null,
                message = "Histogram values must be non-negative.",
                invalidRowIds = negativeRowIds,
            )
        }

        return PlaygroundValidationResult(
            sanitizedEditor = editorState.copy(rows = parsed.sanitizedRows),
            dataModel = PlaygroundDataModel.SimpleSeries(values = values, labels = parsed.labels),
            message = "Applied ${parsed.labels.size} rows.",
        )
    }

    override fun newRowCells(
        rowIndex: Int,
        columns: List<DataEditorColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix = "Bin")

    override fun randomize(editorState: DataEditorState): DataEditorState =
        randomizeEditorValues(
            editorState = editorState,
            valueProvider = { Random.nextFloat() * 80f },
        )

    override fun settingsSchema(session: PlaygroundChartSession): List<SettingDescriptor> =
        listOf(
            SettingDescriptor.Section("Bars"),
            SettingDescriptor.Slider(
                id = "barAlpha",
                label = "Bar Transparency",
                defaultValue = 0.4f,
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
                    val data = it.appliedData as PlaygroundDataModel.SimpleSeries
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
                defaultValue = true,
                read = { style -> (style as BarStyleState).gridVisible },
                write = { style, value -> (style as BarStyleState).copy(gridVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "axisVisible",
                label = "Show Axes",
                defaultValue = true,
                read = { style -> (style as BarStyleState).axisVisible },
                write = { style, value -> (style as BarStyleState).copy(axisVisible = value) },
            ),
            SettingDescriptor.Toggle(
                id = "selectionLineVisible",
                label = "Show Selection Line",
                defaultValue = true,
                read = { style -> (style as BarStyleState).selectionLineVisible },
                write = { style, value -> (style as BarStyleState).copy(selectionLineVisible = value) },
            ),
            SettingDescriptor.Slider(
                id = "selectionLineWidth",
                label = "Selection Line Width",
                defaultValue = 1f,
                min = 0f,
                max = 4f,
                steps = 16,
                read = { style -> (style as BarStyleState).selectionLineWidth },
                write = { style, value -> (style as BarStyleState).copy(selectionLineWidth = value) },
            ),
            SettingDescriptor.Toggle(
                id = "zoomControlsVisible",
                label = "Show Zoom Controls",
                defaultValue = true,
                read = { style -> (style as BarStyleState).zoomControlsVisible },
                write = { style, value -> (style as BarStyleState).copy(zoomControlsVisible = value) },
            ),
        )

    @Composable
    override fun renderPreview(
        session: PlaygroundChartSession,
        modifier: Modifier,
    ) {
        val data = session.appliedData as PlaygroundDataModel.SimpleSeries
        val styleState = session.styleState as BarStyleState
        val dataSet =
            data.values.toChartDataSet(
                title = session.title,
                labels = data.labels,
            )
        val defaultStyle = HistogramChartDefaults.style()
        val normalizedBarColors =
            styleState.barColors?.let { colors ->
                normalizeColorCount(colors = colors, targetCount = data.values.size)
            }
        val style =
            HistogramChartDefaults.style(
                barColor = styleState.barColor ?: defaultStyle.barColor,
                barColors = normalizedBarColors ?: defaultStyle.barColors,
                barAlpha = styleState.barAlpha ?: defaultStyle.barAlpha,
                gridVisible = styleState.gridVisible ?: defaultStyle.gridVisible,
                axisVisible = styleState.axisVisible ?: defaultStyle.axisVisible,
                selectionLineVisible = styleState.selectionLineVisible ?: defaultStyle.selectionLineVisible,
                selectionLineWidth = styleState.selectionLineWidth ?: defaultStyle.selectionLineWidth,
                zoomControlsVisible = styleState.zoomControlsVisible ?: defaultStyle.zoomControlsVisible,
            )
        HistogramChart(dataSet = dataSet, style = style)
    }

    @Composable
    override fun generateCode(session: PlaygroundChartSession): GeneratedSnippet {
        val data = session.appliedData as PlaygroundDataModel.SimpleSeries
        val style = session.styleState as BarStyleState
        val points =
            data.values.mapIndexed { index, value ->
                BarPointInput(
                    label = data.labels?.getOrNull(index) ?: "Bin ${index + 1}",
                    valueText = formatEditorFloat(value.coerceAtLeast(0f)),
                )
            }

        return generator.generate(
            HistogramCodegenConfig(
                points = points,
                title = session.title,
                style = style,
                styleProperties = histogramStylePropertiesSnapshot(style, data.values.size),
                codegenMode = session.codegenMode,
                functionName = deriveFunctionName(session.title, type),
            ),
        )
    }
}
