package codegen.multiline

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.ColorValue
import domain.MultiLineStyleDefaults
import domain.MultiLineStyleState
import domain.normalizeColorCount

fun multiLineStylePropertiesSnapshot(
    styleState: MultiLineStyleState,
    seriesCount: Int,
): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("lineColors", emptyList<ColorValue>()),
            styleProperty("lineAlpha", MultiLineStyleDefaults.lineAlpha),
            styleProperty("bezier", MultiLineStyleDefaults.bezier),
            styleProperty("pointVisible", MultiLineStyleDefaults.pointVisible),
            styleProperty("dragPointVisible", MultiLineStyleDefaults.dragPointVisible),
            styleProperty("pointColor", MultiLineStyleDefaults.pointColor),
            styleProperty("dragPointColor", MultiLineStyleDefaults.pointColor),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty(
                    "lineColors",
                    (styleState.lineColors?.let { normalizeColorCount(it, seriesCount) } ?: emptyList()),
                ),
                styleProperty(
                    "lineAlpha",
                    (
                        styleState.lineAlpha
                            ?: MultiLineStyleDefaults.lineAlpha
                    ),
                ),
                styleProperty("bezier", (styleState.bezier ?: MultiLineStyleDefaults.bezier)),
                styleProperty("pointVisible", (styleState.pointVisible ?: MultiLineStyleDefaults.pointVisible)),
                styleProperty(
                    "dragPointVisible",
                    (styleState.dragPointVisible ?: MultiLineStyleDefaults.dragPointVisible),
                ),
                styleProperty("pointColor", (styleState.pointColor ?: MultiLineStyleDefaults.pointColor)),
                styleProperty("dragPointColor", (styleState.dragPointColor ?: MultiLineStyleDefaults.pointColor)),
            ),
        defaults = defaults,
    )
}
