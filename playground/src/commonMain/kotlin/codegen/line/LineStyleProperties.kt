package codegen.line

import codegen.StylePropertiesSnapshot
import codegen.styleProperty
import domain.LineStyleDefaults
import domain.LineStyleState

fun lineStylePropertiesSnapshot(styleState: LineStyleState): StylePropertiesSnapshot {
    val defaults =
        listOf(
            styleProperty("lineColor", LineStyleDefaults.lineColor),
            styleProperty("lineAlpha", LineStyleDefaults.lineAlpha),
            styleProperty("bezier", LineStyleDefaults.bezier),
            styleProperty("pointColor", LineStyleDefaults.pointColor),
            styleProperty("pointVisible", LineStyleDefaults.pointVisible),
            styleProperty("pointSize", LineStyleDefaults.pointSize),
            styleProperty("dragPointColor", LineStyleDefaults.pointColor),
            styleProperty("dragPointVisible", LineStyleDefaults.dragPointVisible),
            styleProperty("dragPointSize", LineStyleDefaults.dragPointSize),
            styleProperty("dragActivePointSize", LineStyleDefaults.dragActivePointSize),
            styleProperty("axisVisible", LineStyleDefaults.axisVisible),
            styleProperty("axisLineWidth", LineStyleDefaults.axisLineWidth),
            styleProperty("xAxisLabelsVisible", LineStyleDefaults.xAxisLabelsVisible),
            styleProperty("yAxisLabelsVisible", LineStyleDefaults.yAxisLabelsVisible),
            styleProperty("zoomControlsVisible", LineStyleDefaults.zoomControlsVisible),
        )
    return StylePropertiesSnapshot(
        current =
            listOf(
                styleProperty("lineColor", (styleState.lineColor ?: LineStyleDefaults.lineColor)),
                styleProperty("lineAlpha", (styleState.lineAlpha ?: LineStyleDefaults.lineAlpha)),
                styleProperty("bezier", (styleState.bezier ?: LineStyleDefaults.bezier)),
                styleProperty("pointColor", (styleState.pointColor ?: LineStyleDefaults.pointColor)),
                styleProperty("pointVisible", (styleState.pointVisible ?: LineStyleDefaults.pointVisible)),
                styleProperty("pointSize", (styleState.pointSize ?: LineStyleDefaults.pointSize)),
                styleProperty("dragPointColor", (styleState.dragPointColor ?: LineStyleDefaults.pointColor)),
                styleProperty("dragPointVisible", (styleState.dragPointVisible ?: LineStyleDefaults.dragPointVisible)),
                styleProperty("dragPointSize", (styleState.dragPointSize ?: LineStyleDefaults.dragPointSize)),
                styleProperty(
                    "dragActivePointSize",
                    (styleState.dragActivePointSize ?: LineStyleDefaults.dragActivePointSize),
                ),
                styleProperty("axisVisible", (styleState.axisVisible ?: LineStyleDefaults.axisVisible)),
                styleProperty("axisLineWidth", (styleState.axisLineWidth ?: LineStyleDefaults.axisLineWidth)),
                styleProperty(
                    "xAxisLabelsVisible",
                    (styleState.xAxisLabelsVisible ?: LineStyleDefaults.xAxisLabelsVisible),
                ),
                styleProperty(
                    "yAxisLabelsVisible",
                    (styleState.yAxisLabelsVisible ?: LineStyleDefaults.yAxisLabelsVisible),
                ),
                styleProperty(
                    "zoomControlsVisible",
                    (styleState.zoomControlsVisible ?: LineStyleDefaults.zoomControlsVisible),
                ),
            ),
        defaults = defaults,
    )
}
