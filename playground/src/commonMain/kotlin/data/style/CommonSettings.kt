package data.style

import domain.ChartData
import domain.StyleKind
import io.github.hdcharts.core.style.AxesStyle
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** "Axes" and "Grid" sections for the shared Cartesian [AxesStyle] at `axes`. */
internal fun <S : Any> StyleSettingsScope<S>.axesSettings(axes: (S) -> AxesStyle) {
    section("Axes", toggle = toggle("axes.visible", "Show Axes") { axes(it).visible }) {
        +toggle("axes.lines.visible", "Show Axis Lines") { axes(it).lines.visible }
        val linesOn = whenOn("axes.lines.visible")
        +color("axes.lines.color", "Axis Line Color", visibleWhen = linesOn) { axes(it).lines.color }
        +slider("axes.lines.lineWidth", "Width", StyleKind.DP, 0f..4f, 0.25f, visibleWhen = linesOn) {
            axes(it).lines.lineWidth
        }
        listOf(Triple("xLabels", "X", AxesStyle::xLabels), Triple("yLabels", "Y", AxesStyle::yLabels))
            .forEach { (key, name, labels) ->
                val base = "axes.$key"
                +toggle("$base.visible", "Show $name Labels") { labels(axes(it)).visible }
                +color("$base.color", "$name Label Color", visibleWhen = whenOn("$base.visible")) {
                    labels(axes(it)).color
                }
            }
    }
    section(
        "Grid",
        toggle = toggle("axes.grid.visible", "Show Grid") { axes(it).grid.visible },
        visibleWhen = whenOn("axes.visible"),
    ) {
        +slider("axes.grid.steps", "Lines", StyleKind.INT, 1f..10f, 1f) { axes(it).grid.steps }
        +color("axes.grid.color", "Color") { axes(it).grid.color }
    }
}

/**
 * A "Fixed Range" section for the Y axis. While it is off the library derives the range from the
 * data; while on, each bound left unset is still derived. Slider bounds follow the data.
 */
internal fun <S : Any> StyleSettingsScope<S>.fixedRangeSection(
    values: (ChartData) -> List<Float>,
    includeZero: Boolean,
    minDefault: (S) -> Double?,
    maxDefault: (S) -> Double?,
) {
    fun dataMin(data: ChartData): Float = values(data).minOrNull()?.let { if (includeZero) min(it, 0f) else it } ?: 0f

    fun dataMax(data: ChartData): Float = values(data).maxOrNull()?.let { if (includeZero) max(it, 0f) else it } ?: 1f

    val bounds: (ChartData) -> ClosedFloatingPointRange<Float> = { data ->
        val low = dataMin(data)
        val high = dataMax(data)
        val span = max(high - low, max(abs(high), 1f))
        floor(low - span)..ceil(high + span)
    }
    section("Y-Axis Range", toggle = localToggle("range.fixed", "Fixed Range", default = false)) {
        +slider(
            path = "range.min",
            label = "Min",
            kind = StyleKind.DOUBLE,
            range = 0f..1f,
            step = 1f,
            dataDefault = ::dataMin,
            dataRange = bounds,
        ) { minDefault(it) }
        +slider(
            path = "range.max",
            label = "Max",
            kind = StyleKind.DOUBLE,
            range = 0f..1f,
            step = 1f,
            dataDefault = ::dataMax,
            dataRange = bounds,
        ) { maxDefault(it) }
    }
}

internal val ChartData.itemValues: List<Float>
    get() =
        when (this) {
            is ChartData.SingleSeries -> values
            is ChartData.MultiSeries -> series.flatMap { it.values }
        }

internal val singleSeriesCount: (ChartData) -> Int = { data -> (data as ChartData.SingleSeries).values.size }

internal val multiSeriesCount: (ChartData) -> Int = { data -> (data as ChartData.MultiSeries).series.size }
