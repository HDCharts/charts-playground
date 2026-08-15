package codegen.pie

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.PieStyleDefaults
import domain.PieStyleState

fun pieStylePropertiesSnapshot(styleState: PieStyleState): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("donutPercentage", PieStyleDefaults.donutPercentage),
            styleProperty("pieAlpha", PieStyleDefaults.pieAlpha),
            styleProperty("borderWidth", PieStyleDefaults.borderWidth),
            styleProperty("legendVisible", PieStyleDefaults.legendVisible),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty("donutPercentage", (styleState.donutPercentage ?: PieStyleDefaults.donutPercentage)),
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
