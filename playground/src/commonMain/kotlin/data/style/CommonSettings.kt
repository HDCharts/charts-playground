package data.style

import domain.ChartData
import domain.StyleKind
import io.github.hdcharts.charts.style.AxisLabelStyle
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** Show/count/color settings for the X and Y labels under [axisPath], e.g. `axis`. */
internal fun <S : Any> StyleSettingsScope<S>.axisLabelSettings(
    axisPath: String,
    xLabels: (S) -> AxisLabelStyle,
    yLabels: (S) -> AxisLabelStyle,
) {
    listOf(Triple("xLabels", "X", xLabels), Triple("yLabels", "Y", yLabels)).forEach { (key, name, labels) ->
        val base = "$axisPath.$key"
        +toggle("$base.visible", "Show $name Labels") { labels(it).visible }
        +slider(
            path = "$base.count",
            label = "$name Label Count",
            kind = StyleKind.INT,
            range = 2f..12f,
            step = 1f,
            visibleWhen = whenOn("$base.visible"),
        ) { labels(it).count }
        +color("$base.color", "$name Label Color", visibleWhen = whenOn("$base.visible")) { labels(it).color }
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
