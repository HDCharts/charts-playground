package codegen.common

import codegen.StylePropertiesSnapshot
import codegen.StyleProperty

data class RenderedStyleArgument(
    val code: String,
    val additionalImports: Set<String> = emptySet(),
)

fun resolveStyleArguments(
    styleProperties: StylePropertiesSnapshot?,
    styleBuilder: String = "",
): List<RenderedStyleArgument> {
    if (styleProperties == null) {
        return emptyList()
    }

    val defaultsByName = styleProperties.defaults.associate { property -> property.name to property.value }
    val mappings = groupedMappings(styleBuilder)
    if (mappings.isEmpty()) {
        return styleProperties.current.sortedBy(StyleProperty::name).mapNotNull { property ->
            val currentValue = property.value
            if (currentValue == defaultsByName[property.name]) return@mapNotNull null
            val literal = toKotlinLiteral(propertyName = property.name, value = currentValue)
            RenderedStyleArgument(
                code = "${property.name} = ${literal.code},",
                additionalImports = literal.additionalImports,
            )
        }
    }

    val factoryPrefix = styleBuilder.substringBeforeLast(".")
    val grouped = mutableMapOf<String, MutableList<Pair<String, KotlinLiteral>>>()
    val direct = mutableListOf<RenderedStyleArgument>()
    styleProperties.current.sortedBy(StyleProperty::name).forEach { property ->
        val name = property.name
        val currentValue = property.value
        val defaultValue = defaultsByName[name]
        if (currentValue == defaultValue) {
            return@forEach
        }
        val mapping = mappings[name] ?: return@forEach
        val literal = toStyleLiteral(name, currentValue, mapping.dp)
        if (mapping.group == null) {
            direct +=
                RenderedStyleArgument(
                    code = "$name = ${literal.code},",
                    additionalImports = literal.additionalImports,
                )
        } else {
            val groupedLiteral =
                mapping.nestedFactory?.let { nestedFactory ->
                    literal.copy(
                        code =
                            "$factoryPrefix.$nestedFactory(" +
                                "${mapping.nestedArgument} = ${literal.code})",
                    )
                } ?: literal
            grouped.getOrPut(mapping.group) { mutableListOf() } += mapping.argument to groupedLiteral
        }
    }

    val groupedArguments =
        mappings.values
            .mapNotNull { mapping -> mapping.group }
            .distinct()
            .mapNotNull { group ->
                val members = grouped[group] ?: return@mapNotNull null
                val builder = "$factoryPrefix.$group"
                val memberCode = members.joinToString(" ") { (name, literal) -> "$name = ${literal.code}," }
                RenderedStyleArgument(
                    code = "$group = $builder($memberCode),",
                    additionalImports = members.flatMap { it.second.additionalImports }.toSet(),
                )
            }
    return (groupedArguments + direct).sortedBy { argument -> argument.code }
}

private data class StyleMapping(
    val group: String?,
    val argument: String = "",
    val dp: Boolean = false,
    val nestedFactory: String? = null,
    val nestedArgument: String = "visible",
)

private fun toStyleLiteral(
    propertyName: String,
    value: codegen.StylePropertyValue,
    dp: Boolean,
): KotlinLiteral {
    val literal = toKotlinLiteral(propertyName, value)
    if (!dp || value !is codegen.StylePropertyValue.FloatValue) return literal
    return literal.copy(
        code = "${literal.code.removeSuffix("f")}.dp",
        additionalImports = literal.additionalImports + "import androidx.compose.ui.unit.dp",
    )
}

private fun groupedMappings(styleBuilder: String): Map<String, StyleMapping> {
    val prefix = styleBuilder.substringBeforeLast(".")
    return when (prefix) {
        "LineChartDefaults" ->
            mapOf(
                "lineColor" to StyleMapping("line", "color"),
                "lineAlpha" to StyleMapping("line", "alpha"),
                "bezier" to StyleMapping("line", "bezier"),
                "pointColor" to StyleMapping("points", "color"),
                "pointVisible" to StyleMapping("points", "visible"),
                "pointSize" to StyleMapping("points", "size", dp = true),
                "dragPointColor" to StyleMapping("selection", "color"),
                "dragPointVisible" to StyleMapping("selection", "visible"),
                "dragPointSize" to StyleMapping("selection", "size", dp = true),
                "dragActivePointSize" to StyleMapping("selection", "activeSize", dp = true),
                "axisVisible" to StyleMapping("axis", "visible"),
                "axisLineWidth" to StyleMapping("axis", "lineWidth", dp = true),
                "xAxisLabelsVisible" to StyleMapping("axis", "xLabels", nestedFactory = "xLabels"),
                "yAxisLabelsVisible" to StyleMapping("axis", "yLabels", nestedFactory = "yLabels"),
                "zoomControlsVisible" to StyleMapping(null),
                "lineColors" to StyleMapping("line", "colors"),
            )
        "BarChartDefaults", "HistogramChartDefaults" ->
            mapOf(
                "barColor" to StyleMapping("bars", "color"),
                "barColors" to StyleMapping("bars", "colors"),
                "barAlpha" to StyleMapping("bars", "alpha"),
                "gridVisible" to StyleMapping("grid", "visible"),
                "axisVisible" to StyleMapping("axis", "visible"),
                "selectionLineVisible" to StyleMapping("selectionLine", "visible"),
                "selectionLineWidth" to StyleMapping("selectionLine", "width", dp = true),
                "zoomControlsVisible" to StyleMapping(null),
            )
        "StackedBarChartDefaults" ->
            mapOf(
                "barColor" to StyleMapping("segments", "color"),
                "barColors" to StyleMapping("segments", "colors"),
                "barAlpha" to StyleMapping("segments", "alpha"),
                "selectionLineVisible" to StyleMapping("selection", "visible"),
                "selectionLineWidth" to StyleMapping("selection", "width", dp = true),
                "zoomControlsVisible" to StyleMapping(null),
            )
        "StackedAreaChartDefaults" ->
            mapOf(
                "areaColor" to StyleMapping("fill", "color"),
                "areaColors" to StyleMapping("fill", "colors"),
                "fillAlpha" to StyleMapping("fill", "alpha"),
                "lineVisible" to StyleMapping("boundary", "visible"),
                "lineColor" to StyleMapping("boundary", "color"),
                "lineColors" to StyleMapping("boundary", "colors"),
                "lineWidth" to StyleMapping("boundary", "width", dp = true),
                "bezier" to StyleMapping("boundary", "bezier"),
                "zoomControlsVisible" to StyleMapping(null),
            )
        "RadarChartDefaults" ->
            mapOf(
                "lineColors" to StyleMapping("polygon", "lineColors"),
                "lineWidth" to StyleMapping("polygon", "lineWidth"),
                "fillVisible" to StyleMapping("polygon", "fillVisible"),
                "fillAlpha" to StyleMapping("polygon", "fillAlpha"),
                "pointVisible" to StyleMapping("points", "visible"),
                "pointSize" to StyleMapping("points", "size"),
                "gridVisible" to StyleMapping("grid", "visible"),
                "categoryLegendVisible" to StyleMapping("categories", "legendVisible"),
            )
        else -> emptyMap()
    }
}
