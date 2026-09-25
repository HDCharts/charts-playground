package data.charts

import data.style.axisLabelSettings
import data.style.multiSeriesCount
import data.style.styleSettings
import domain.SettingDescriptor
import domain.StyleKind
import io.github.hdcharts.charts.style.StackedBarChartStyle

internal val stackedBarStyleSettings: List<SettingDescriptor> =
    styleSettings<StackedBarChartStyle> {
        section("Bars") {
            +palette(
                "segments.colors",
                "Segment Colors",
                multiSeriesCount,
            ) {
                style,
                count,
                ->
                style.segments.resolveColors(count)
            }
            +slider("segments.alpha", "Transparency", StyleKind.FLOAT, 0f..1f, 0.05f) { it.segments.alpha }
            +slider("layout.space", "Spacing", StyleKind.DP, 0f..40f, 1f) { it.layout.space }
            +slider("layout.minBarWidth", "Min Bar Width", StyleKind.DP, 2f..40f, 1f) { it.layout.minBarWidth }
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
