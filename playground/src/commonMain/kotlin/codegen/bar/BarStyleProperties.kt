package codegen.bar

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.BarStyleDefaults
import domain.BarStyleState
import domain.ColorValue
import domain.normalizeColorCount

fun barStylePropertiesSnapshot(
    styleState: BarStyleState,
    seriesCount: Int,
): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("barColor", BarStyleDefaults.barColor),
            styleProperty("barColors", emptyList<ColorValue>()),
            styleProperty("barAlpha", BarStyleDefaults.barAlpha),
            styleProperty("gridVisible", BarStyleDefaults.gridVisible),
            styleProperty("axisVisible", BarStyleDefaults.axisVisible),
            styleProperty("selectionLineVisible", BarStyleDefaults.selectionLineVisible),
            styleProperty("selectionLineWidth", BarStyleDefaults.selectionLineWidth),
            styleProperty("zoomControlsVisible", BarStyleDefaults.zoomControlsVisible),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty("barColor", (styleState.barColor ?: BarStyleDefaults.barColor)),
                styleProperty(
                    "barColors",
                    (styleState.barColors?.let { normalizeColorCount(it, seriesCount) } ?: emptyList()),
                ),
                styleProperty(
                    "barAlpha",
                    (
                        styleState.barAlpha
                            ?: BarStyleDefaults.barAlpha
                    ),
                ),
                styleProperty("gridVisible", (styleState.gridVisible ?: BarStyleDefaults.gridVisible)),
                styleProperty("axisVisible", (styleState.axisVisible ?: BarStyleDefaults.axisVisible)),
                styleProperty(
                    "selectionLineVisible",
                    (styleState.selectionLineVisible ?: BarStyleDefaults.selectionLineVisible),
                ),
                styleProperty(
                    "selectionLineWidth",
                    (
                        styleState.selectionLineWidth
                            ?: BarStyleDefaults.selectionLineWidth
                    ),
                ),
                styleProperty(
                    "zoomControlsVisible",
                    (styleState.zoomControlsVisible ?: BarStyleDefaults.zoomControlsVisible),
                ),
            ),
        defaults = defaults,
    )
}
