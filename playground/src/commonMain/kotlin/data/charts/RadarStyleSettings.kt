package data.charts

import data.style.multiSeriesCount
import data.style.styleSettings
import data.style.whenOff
import data.style.whenOn
import domain.SettingDescriptor
import domain.StyleKind
import io.github.hdcharts.charts.style.RadarChartStyle

internal val radarStyleSettings: List<SettingDescriptor> =
    styleSettings<RadarChartStyle> {
        section("Lines") {
            +palette("polygon.lineColors", "Colors", multiSeriesCount) {
                style,
                count,
                ->
                style.polygon.resolveLineColors(count)
            }
            +slider("polygon.lineWidth", "Width", StyleKind.FLOAT, 0f..8f, 0.5f) { it.polygon.lineWidth }
        }
        section("Fill", toggle = toggle("polygon.fillVisible", "Show Fill") { it.polygon.fillVisible }) {
            +slider("polygon.fillAlpha", "Transparency", StyleKind.FLOAT, 0f..1f, 0.05f) { it.polygon.fillAlpha }
        }
        section("Points", toggle = toggle("points.visible", "Show Points") { it.points.visible }) {
            +slider("points.size", "Size", StyleKind.FLOAT, 0f..12f, 1f) { it.points.size }
            +toggle("points.colorSameAsLine", "Match Line Color") { it.points.colorSameAsLine }
            +color("points.color", "Color", visibleWhen = whenOff("points.colorSameAsLine")) { it.points.color }
        }
        // Axis labels are drawn independently of the axis lines.
        section("Axes") {
            +toggle("axes.visible", "Show Axis Lines") { it.axes.visible }
            +color("axes.lineColor", "Axis Line Color", visibleWhen = whenOn("axes.visible")) { it.axes.lineColor }
            +toggle("axes.labelVisible", "Show Axis Labels") { it.axes.labelVisible }
            +color("axes.labelColor", "Label Color", visibleWhen = whenOn("axes.labelVisible")) { it.axes.labelColor }
        }
        section("Grid", toggle = toggle("grid.visible", "Show Grid") { it.grid.visible }) {
            +slider("grid.steps", "Rings", StyleKind.INT, 1f..10f, 1f) { it.grid.steps }
            +color("grid.color", "Color") { it.grid.color }
        }
        section("Categories") {
            +toggle("categories.legendVisible", "Show Category Legend") { it.categories.legendVisible }
            +toggle("categories.pinsVisible", "Show Category Pins") { it.categories.pinsVisible }
        }
    }
