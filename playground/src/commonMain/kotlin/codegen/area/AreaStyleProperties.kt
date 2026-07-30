package codegen.area

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.AreaStyleDefaults
import domain.AreaStyleState
import domain.ColorValue
import domain.normalizeColorCount

fun areaStylePropertiesSnapshot(
    styleState: AreaStyleState,
    seriesCount: Int,
): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("areaColor", AreaStyleDefaults.areaColor),
            styleProperty("areaColors", emptyList<ColorValue>()),
            styleProperty("fillAlpha", AreaStyleDefaults.fillAlpha),
            styleProperty("lineVisible", AreaStyleDefaults.lineVisible),
            styleProperty("lineColor", AreaStyleDefaults.lineColor),
            styleProperty("lineColors", emptyList<ColorValue>()),
            styleProperty("lineWidth", AreaStyleDefaults.lineWidth),
            styleProperty("bezier", AreaStyleDefaults.bezier),
            styleProperty("zoomControlsVisible", AreaStyleDefaults.zoomControlsVisible),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty("areaColor", AreaStyleDefaults.areaColor),
                styleProperty(
                    "areaColors",
                    (styleState.areaColors?.let { normalizeColorCount(it, seriesCount) } ?: emptyList()),
                ),
                styleProperty(
                    "fillAlpha",
                    (
                        styleState.fillAlpha
                            ?: AreaStyleDefaults.fillAlpha
                    ),
                ),
                styleProperty("lineVisible", (styleState.lineVisible ?: AreaStyleDefaults.lineVisible)),
                styleProperty("lineColor", AreaStyleDefaults.lineColor),
                styleProperty(
                    "lineColors",
                    (styleState.lineColors?.let { normalizeColorCount(it, seriesCount) } ?: emptyList()),
                ),
                styleProperty(
                    "lineWidth",
                    (
                        styleState.lineWidth
                            ?: AreaStyleDefaults.lineWidth
                    ),
                ),
                styleProperty("bezier", (styleState.bezier ?: AreaStyleDefaults.bezier)),
                styleProperty(
                    "zoomControlsVisible",
                    (styleState.zoomControlsVisible ?: AreaStyleDefaults.zoomControlsVisible),
                ),
            ),
        defaults = defaults,
    )
}
