package codegen.pie

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.ColorValue
import domain.PieStyleDefaults
import domain.PieStyleState
import domain.normalizeColorCount

fun pieStylePropertiesSnapshot(
    styleState: PieStyleState,
    itemCount: Int,
): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("donutPercentage", PieStyleDefaults.donutPercentage),
            styleProperty("pieColors", emptyList<ColorValue>()),
            styleProperty("pieAlpha", PieStyleDefaults.pieAlpha),
            styleProperty("borderWidth", PieStyleDefaults.borderWidth),
            styleProperty("legendVisible", PieStyleDefaults.legendVisible),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty("donutPercentage", (styleState.donutPercentage ?: PieStyleDefaults.donutPercentage)),
                styleProperty(
                    "pieColors",
                    (styleState.pieColors?.let { normalizeColorCount(it, itemCount) } ?: emptyList()),
                ),
                styleProperty(
                    "pieAlpha",
                    (
                        styleState.pieAlpha
                            ?: PieStyleDefaults.pieAlpha
                    ),
                ),
                styleProperty("borderWidth", (styleState.borderWidth ?: PieStyleDefaults.borderWidth)),
                styleProperty("legendVisible", (styleState.legendVisible ?: PieStyleDefaults.legendVisible)),
            ),
        defaults = defaults,
    )
}
