package codegen.radar

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.ColorValue
import domain.RadarStyleDefaults
import domain.RadarStyleState
import domain.normalizeColorCount

fun radarStylePropertiesSnapshot(
    styleState: RadarStyleState,
    seriesCount: Int,
): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("lineColors", emptyList<ColorValue>()),
            styleProperty("lineWidth", RadarStyleDefaults.lineWidth),
            styleProperty("pointVisible", RadarStyleDefaults.pointVisible),
            styleProperty("pointSize", RadarStyleDefaults.pointSize),
            styleProperty("fillVisible", RadarStyleDefaults.fillVisible),
            styleProperty("fillAlpha", RadarStyleDefaults.fillAlpha),
            styleProperty("gridVisible", RadarStyleDefaults.gridVisible),
            styleProperty("categoryLegendVisible", RadarStyleDefaults.categoryLegendVisible),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty(
                    "lineColors",
                    (styleState.lineColors?.let { normalizeColorCount(it, seriesCount) } ?: emptyList()),
                ),
                styleProperty(
                    "lineWidth",
                    (
                        styleState.lineWidth
                            ?: RadarStyleDefaults.lineWidth
                    ),
                ),
                styleProperty("pointVisible", (styleState.pointVisible ?: RadarStyleDefaults.pointVisible)),
                styleProperty("pointSize", (styleState.pointSize ?: RadarStyleDefaults.pointSize)),
                styleProperty("fillVisible", (styleState.fillVisible ?: RadarStyleDefaults.fillVisible)),
                styleProperty("fillAlpha", (styleState.fillAlpha ?: RadarStyleDefaults.fillAlpha)),
                styleProperty("gridVisible", (styleState.gridVisible ?: RadarStyleDefaults.gridVisible)),
                styleProperty(
                    "categoryLegendVisible",
                    (styleState.categoryLegendVisible ?: RadarStyleDefaults.categoryLegendVisible),
                ),
            ),
        defaults = defaults,
    )
}
