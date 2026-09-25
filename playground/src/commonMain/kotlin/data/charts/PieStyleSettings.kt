package data.charts

import data.style.percentFormat
import data.style.singleSeriesCount
import data.style.styleSettings
import domain.PIE_SLICE_COLORS_PATH
import domain.SettingDescriptor
import domain.StyleKind
import domain.StyleTarget
import io.github.hdcharts.charts.style.PieChartStyle

internal val pieStyleSettings: List<SettingDescriptor> =
    styleSettings<PieChartStyle> {
        section("Slices") {
            +palette(PIE_SLICE_COLORS_PATH, "Colors", singleSeriesCount, target = StyleTarget.DATA) { style, count ->
                style.slices.resolveColors(count)
            }
            +slider("slices.alpha", "Transparency", StyleKind.FLOAT, 0f..1f, 0.05f) { it.slices.alpha }
        }
        section("Shape") {
            +slider(
                path = "donut.holePercentage",
                label = "Donut Hole Size",
                kind = StyleKind.FLOAT,
                range = 0f..70f,
                step = 3.5f,
                format = percentFormat,
            ) { it.donut.holePercentage }
            +slider("border.width", "Border Width", StyleKind.DP, 0f..10f, 0.5f) { it.border.width }
            +color("border.color", "Border Color") { it.border.color }
        }
        section("Legend") {
            +toggle("legend.visible", "Show Legend") { it.legend.visible }
        }
    }
