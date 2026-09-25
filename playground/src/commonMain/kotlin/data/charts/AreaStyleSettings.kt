package data.charts

import data.style.axisLabelSettings
import data.style.curveOptions
import data.style.multiSeriesCount
import data.style.styleSettings
import domain.SettingDescriptor
import domain.StyleKind
import io.github.hdcharts.charts.style.StackedAreaChartStyle

internal val areaStyleSettings: List<SettingDescriptor> =
    styleSettings<StackedAreaChartStyle> {
        section("Areas") {
            +palette("fill.colors", "Colors", multiSeriesCount) { style, count -> style.fill.resolveColors(count) }
            +slider("fill.alpha", "Transparency", StyleKind.FLOAT, 0f..1f, 0.05f) { it.fill.alpha }
            // The curve shapes the filled areas too, so it stays here rather than under Boundary Lines.
            +choice("boundary.bezier", "Curve", curveOptions) { it.boundary.bezier }
        }
        section("Boundary Lines", toggle = toggle("boundary.visible", "Show Boundary Lines") { it.boundary.visible }) {
            +palette("boundary.colors", "Colors", multiSeriesCount) { style, count ->
                style.boundary.resolveColors(count)
            }
            +slider("boundary.width", "Width", StyleKind.DP, 0f..8f, 0.5f) { it.boundary.width }
        }
        section("Axes") {
            axisLabelSettings("axis", xLabels = { it.axis.xLabels }, yLabels = { it.axis.yLabels })
        }
        section(
            "Selection Line",
            toggle = toggle("selection.visible", "Show Selection Line") { it.selection.visible },
        ) {
            +slider("selection.width", "Width", StyleKind.DP, 0f..4f, 0.25f) { it.selection.width }
            +color("selection.color", "Color") { it.selection.color }
        }
        section("Controls") {
            +toggle("zoomControlsVisible", "Show Zoom Controls") { it.zoomControlsVisible }
        }
    }
