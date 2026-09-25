package data.charts

import data.style.fixedRangeSection
import data.style.itemValues
import data.style.multiSeriesCount
import data.style.styleSettings
import domain.SettingDescriptor
import io.github.hdcharts.charts.style.LineChartStyle

internal val multiLineStyleSettings: List<SettingDescriptor> =
    styleSettings<LineChartStyle> {
        section("Lines") {
            +palette("line.colors", "Colors", multiSeriesCount) { style, count -> style.line.resolveColors(count) }
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
        section("Legend") {
            +toggle("legend.visible", "Show Legend") { it.legend.visible }
        }
        section("Controls") {
            +toggle("zoomControlsVisible", "Show Zoom Controls") { it.zoomControlsVisible }
        }
    }
