package data.charts

import data.style.StyleSettingsScope
import data.style.axisLabelSettings
import data.style.fixedRangeSection
import data.style.itemValues
import data.style.singleSeriesCount
import data.style.styleSettings
import data.style.whenOn
import domain.SettingDescriptor
import domain.StyleKind
import io.github.hdcharts.charts.style.BarAxisStyle
import io.github.hdcharts.charts.style.BarBarsStyle
import io.github.hdcharts.charts.style.BarChartStyle
import io.github.hdcharts.charts.style.BarGridStyle
import io.github.hdcharts.charts.style.BarRangeStyle
import io.github.hdcharts.charts.style.BarSelectionLineStyle
import io.github.hdcharts.charts.style.HistogramChartStyle

internal val barStyleSettings: List<SettingDescriptor> =
    styleSettings<BarChartStyle> {
        barSettings(
            bars = { it.bars },
            range = { it.range },
            grid = { it.grid },
            axis = { it.axis },
            selectionLine = { it.selectionLine },
            zoomControlsVisible = { it.zoomControlsVisible },
        )
    }

internal val histogramStyleSettings: List<SettingDescriptor> =
    styleSettings<HistogramChartStyle> {
        barSettings(
            bars = { it.bars },
            range = { it.range },
            grid = { it.grid },
            axis = { it.axis },
            selectionLine = { it.selectionLine },
            zoomControlsVisible = { it.zoomControlsVisible },
        )
    }

/** Bar and histogram styles share the same blocks; only the style class differs. */
private fun <S : Any> StyleSettingsScope<S>.barSettings(
    bars: (S) -> BarBarsStyle,
    range: (S) -> BarRangeStyle,
    grid: (S) -> BarGridStyle,
    axis: (S) -> BarAxisStyle,
    selectionLine: (S) -> BarSelectionLineStyle,
    zoomControlsVisible: (S) -> Boolean,
) {
    section("Bars") {
        +color("bars.color", "Color") { bars(it).color }
        +palette("bars.colors", "Per-Bar Colors", singleSeriesCount) { style, count ->
            bars(style).resolveColors(count)
        }
        +slider("bars.alpha", "Transparency", StyleKind.FLOAT, 0f..1f, 0.05f) { bars(it).alpha }
        +slider("bars.space", "Spacing", StyleKind.DP, 0f..40f, 1f) { bars(it).space }
        +slider("bars.minBarWidth", "Min Bar Width", StyleKind.DP, 2f..40f, 1f) { bars(it).minBarWidth }
    }
    section("Axes") {
        +toggle("axis.visible", "Show Axis Lines") { axis(it).visible }
        +color("axis.color", "Axis Line Color", visibleWhen = whenOn("axis.visible")) { axis(it).color }
        axisLabelSettings("axis", xLabels = { axis(it).xLabels }, yLabels = { axis(it).yLabels })
    }
    section("Grid", toggle = toggle("grid.visible", "Show Grid") { grid(it).visible }) {
        +slider("grid.steps", "Lines", StyleKind.INT, 1f..10f, 1f) { grid(it).steps }
        +color("grid.color", "Color") { grid(it).color }
    }
    fixedRangeSection(
        values = { it.itemValues },
        includeZero = true,
        minDefault = { range(it).min },
        maxDefault = { range(it).max },
    )
    section(
        "Selection Line",
        toggle = toggle("selectionLine.visible", "Show Selection Line") { selectionLine(it).visible },
    ) {
        +slider("selectionLine.width", "Width", StyleKind.DP, 0f..4f, 0.25f) { selectionLine(it).width }
        +color("selectionLine.color", "Color") { selectionLine(it).color }
    }
    section("Controls") {
        +toggle("zoomControlsVisible", "Show Zoom Controls") { zoomControlsVisible(it) }
    }
}
