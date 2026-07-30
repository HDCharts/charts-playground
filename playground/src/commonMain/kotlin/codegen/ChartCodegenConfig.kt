package codegen

import domain.AREA_CHART_TITLE
import domain.BAR_CHART_TITLE
import domain.CodegenMode
import domain.HISTOGRAM_CHART_TITLE
import domain.LINE_CHART_TITLE
import domain.MULTI_LINE_CHART_TITLE
import domain.PIE_CHART_TITLE
import domain.RADAR_CHART_TITLE
import domain.STACKED_BAR_CHART_TITLE

data class PieSliceInput(
    val label: String,
    val valueText: String,
)

typealias LinePointInput = PieSliceInput
typealias BarPointInput = PieSliceInput

data class MultiSeriesCodegenInput(
    val label: String,
    val values: List<Float>,
)

data class PieCodegenConfig(
    val rows: List<PieSliceInput>,
    val title: String = PIE_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundPieChartExample",
)

data class LineCodegenConfig(
    val points: List<LinePointInput>,
    val title: String = LINE_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundLineChartExample",
)

data class BarCodegenConfig(
    val points: List<BarPointInput>,
    val title: String = BAR_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundBarChartExample",
)

data class HistogramCodegenConfig(
    val points: List<BarPointInput>,
    val title: String = HISTOGRAM_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundHistogramChartExample",
)

data class MultiLineCodegenConfig(
    val series: List<MultiSeriesCodegenInput>,
    val categories: List<String>,
    val title: String = MULTI_LINE_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundMultiLineChartExample",
)

data class StackedBarCodegenConfig(
    val series: List<MultiSeriesCodegenInput>,
    val categories: List<String>,
    val title: String = STACKED_BAR_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundStackedBarChartExample",
)

data class AreaCodegenConfig(
    val series: List<MultiSeriesCodegenInput>,
    val categories: List<String>,
    val title: String = AREA_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundAreaChartExample",
)

data class RadarCodegenConfig(
    val series: List<MultiSeriesCodegenInput>,
    val categories: List<String>,
    val title: String = RADAR_CHART_TITLE,
    val styleProperties: StylePropertiesSnapshot? = null,
    val codegenMode: CodegenMode = CodegenMode.MINIMAL,
    val functionName: String = "PlaygroundRadarChartExample",
)
