package data.charts

import data.style.axesSettings
import data.style.fixedRangeSection
import data.style.itemValues
import data.style.multiSeriesCount
import data.style.styleSettings
import domain.SettingDescriptor
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.line.LineChartStyle

@OptIn(InternalChartsApi::class)
internal val multiLineStyleSettings: List<SettingDescriptor> =
    styleSettings<LineChartStyle> {
        section("Lines") {
            +palette("line.colors", "Colors", multiSeriesCount) { style, count ->
                resolvePaletteColors(
                    baseColor = style.line.color,
                    colors = style.line.colors,
                    count = count,
                    singleItemUsesBase = true,
                )
            }
            lineAppearance()
        }
        pointAndSelectionSections()
        axesSettings { it.axes }
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
