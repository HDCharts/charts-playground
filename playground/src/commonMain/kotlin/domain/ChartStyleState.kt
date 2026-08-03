package domain

sealed interface ChartStyleState

data class PieStyleState(
    val donutPercentage: Float? = null,
    val borderWidth: Float? = null,
    val pieAlpha: Float? = null,
    val legendVisible: Boolean? = null,
    val pieColors: List<ColorValue>? = null,
) : ChartStyleState

data class LineStyleState(
    val lineColor: ColorValue? = null,
    val lineAlpha: Float? = null,
    val bezier: Boolean? = null,
    val pointColor: ColorValue? = null,
    val pointVisible: Boolean? = null,
    val pointSize: Float? = null,
    val dragPointColor: ColorValue? = null,
    val dragPointVisible: Boolean? = null,
    val dragPointSize: Float? = null,
    val dragActivePointSize: Float? = null,
    val axisVisible: Boolean? = null,
    val axisLineWidth: Float? = null,
    val xAxisLabelsVisible: Boolean? = null,
    val yAxisLabelsVisible: Boolean? = null,
    val zoomControlsVisible: Boolean? = null,
) : ChartStyleState

data class MultiLineStyleState(
    val lineColors: List<ColorValue>? = null,
    val lineAlpha: Float? = null,
    val bezier: Boolean? = null,
    val pointVisible: Boolean? = null,
    val dragPointVisible: Boolean? = null,
    val pointColor: ColorValue? = null,
    val dragPointColor: ColorValue? = null,
) : ChartStyleState

data class BarStyleState(
    val barColor: ColorValue? = null,
    val barColors: List<ColorValue>? = null,
    val barAlpha: Float? = null,
    val gridVisible: Boolean? = null,
    val axisVisible: Boolean? = null,
    val selectionLineVisible: Boolean? = null,
    val selectionLineWidth: Float? = null,
    val zoomControlsVisible: Boolean? = null,
) : ChartStyleState

data class StackedBarStyleState(
    val barColors: List<ColorValue>? = null,
    val barAlpha: Float? = null,
    val selectionLineVisible: Boolean? = null,
    val selectionLineWidth: Float? = null,
    val zoomControlsVisible: Boolean? = null,
) : ChartStyleState

data class AreaStyleState(
    val areaColors: List<ColorValue>? = null,
    val lineColors: List<ColorValue>? = null,
    val fillAlpha: Float? = null,
    val lineVisible: Boolean? = null,
    val lineWidth: Float? = null,
    val bezier: Boolean? = null,
    val zoomControlsVisible: Boolean? = null,
) : ChartStyleState

data class RadarStyleState(
    val lineColors: List<ColorValue>? = null,
    val lineWidth: Float? = null,
    val pointVisible: Boolean? = null,
    val pointSize: Float? = null,
    val fillVisible: Boolean? = null,
    val fillAlpha: Float? = null,
    val gridVisible: Boolean? = null,
    val categoryLegendVisible: Boolean? = null,
) : ChartStyleState
