package codegen.stackedbar

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.ColorValue
import domain.StackedBarStyleDefaults
import domain.StackedBarStyleState
import domain.normalizeColorCount

fun stackedBarStylePropertiesSnapshot(
    styleState: StackedBarStyleState,
    seriesCount: Int,
): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("barColor", StackedBarStyleDefaults.barColor),
            styleProperty("barAlpha", StackedBarStyleDefaults.barAlpha),
            styleProperty("barColors", emptyList<ColorValue>()),
            styleProperty("zoomControlsVisible", StackedBarStyleDefaults.zoomControlsVisible),
            styleProperty("selectionLineVisible", StackedBarStyleDefaults.selectionLineVisible),
            styleProperty("selectionLineWidth", StackedBarStyleDefaults.selectionLineWidth),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty("barColor", StackedBarStyleDefaults.barColor),
                styleProperty("barAlpha", (styleState.barAlpha ?: StackedBarStyleDefaults.barAlpha)),
                styleProperty(
                    "barColors",
                    (styleState.barColors?.let { normalizeColorCount(it, seriesCount) } ?: emptyList()),
                ),
                styleProperty(
                    "zoomControlsVisible",
                    (
                        styleState.zoomControlsVisible
                            ?: StackedBarStyleDefaults.zoomControlsVisible
                    ),
                ),
                styleProperty(
                    "selectionLineVisible",
                    (styleState.selectionLineVisible ?: StackedBarStyleDefaults.selectionLineVisible),
                ),
                styleProperty(
                    "selectionLineWidth",
                    (styleState.selectionLineWidth ?: StackedBarStyleDefaults.selectionLineWidth),
                ),
            ),
        defaults = defaults,
    )
}
