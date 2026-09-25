package data.charts

import data.style.StyleSettingsScope
import data.style.axisLabelSettings
import data.style.curveOptions
import data.style.fixedRangeSection
import data.style.itemValues
import data.style.styleSettings
import data.style.whenAnyOn
import data.style.whenOn
import domain.SettingDescriptor
import domain.StyleKind
import io.github.hdcharts.charts.style.LineChartStyle

internal val lineStyleSettings: List<SettingDescriptor> =
    styleSettings<LineChartStyle> {
        section("Line") {
            +color("line.color", "Color") { it.line.color }
            lineAppearance()
        }
        pointAndSelectionSections()
        lineAxesSection()
        fixedRangeSection(
            values = { it.itemValues },
            includeZero = false,
            minDefault = { it.range.min },
            maxDefault = { it.range.max },
        )
        section("Controls") {
            +toggle("zoomControlsVisible", "Show Zoom Controls") { it.zoomControlsVisible }
        }
    }

/** Transparency, width and curve, shared by single- and multi-line charts. */
internal fun StyleSettingsScope<LineChartStyle>.lineAppearance() {
    +slider("line.alpha", "Transparency", StyleKind.FLOAT, 0f..1f, 0.05f) { it.line.alpha }
    +slider("line.strokeWidth", "Width", StyleKind.DP, 1f..12f, 0.5f) { it.line.strokeWidth }
    +choice("line.bezier", "Curve", curveOptions) { it.line.bezier }
}

internal fun StyleSettingsScope<LineChartStyle>.pointAndSelectionSections() {
    section("Points", toggle = toggle("points.visible", "Show Points") { it.points.visible }) {
        +slider("points.size", "Size", StyleKind.DP, 2f..20f, 1f) { it.points.size }
        +color("points.color", "Color") { it.points.color }
    }
    // The selected-point marker is drawn when either points or the drag point are shown.
    val markerVisible = whenAnyOn("points.visible", "selection.visible")
    section("Selection") {
        +toggle("selection.visible", "Show Drag Point") { it.selection.visible }
        +slider(
            path = "selection.size",
            label = "Drag Point Size",
            kind = StyleKind.DP,
            range = 2f..20f,
            step = 1f,
            visibleWhen = whenOn("selection.visible"),
        ) { it.selection.size }
        +slider(
            path = "selection.activeSize",
            label = "Selected Point Size",
            kind = StyleKind.DP,
            range = 2f..24f,
            step = 1f,
            visibleWhen = markerVisible,
        ) { it.selection.activeSize }
        +color("selection.color", "Marker Color", visibleWhen = markerVisible) { it.selection.color }
    }
}

internal fun StyleSettingsScope<LineChartStyle>.lineAxesSection() {
    section("Axes") {
        +toggle("axis.visible", "Show Axis Lines") { it.axis.visible }
        +color("axis.color", "Axis Line Color", visibleWhen = whenOn("axis.visible")) { it.axis.color }
        // Also sets the selection line width, so it stays visible when axis lines are hidden.
        +slider("axis.lineWidth", "Axis & Selection Line Width", StyleKind.DP, 0f..4f, 0.25f) { it.axis.lineWidth }
        axisLabelSettings("axis", xLabels = { it.axis.xLabels }, yLabels = { it.axis.yLabels })
    }
}
