package codegen.pie

import codegen.StylePropertiesSnapshot
import codegen.StyleProperty
import codegen.common.KotlinLiteral
import codegen.common.RenderedStyleArgument
import codegen.common.toKotlinLiteral

private val PIE_GROUP_ORDER = listOf("donut", "slices", "border", "legend")

private val PIE_GROUP_BUILDERS =
    mapOf(
        "donut" to "PieChartDefaults.donut",
        "slices" to "PieChartDefaults.slices",
        "border" to "PieChartDefaults.border",
        "legend" to "PieChartDefaults.legend",
    )

private val PIE_PROPERTY_GROUP =
    mapOf(
        "donutPercentage" to ("donut" to "holePercentage"),
        "pieAlpha" to ("slices" to "alpha"),
        "borderWidth" to ("border" to "width"),
        "legendVisible" to ("legend" to "visible"),
    )

/**
 * Resolves a flat [StylePropertiesSnapshot] into grouped pie style arguments.
 *
 * The flat legacy keys (e.g. `donutPercentage`) are mapped onto the grouped
 * `PieChartDefaults` factories so generated snippets use the grouped API. Slice colors
 * are emitted on each `PieSlice` rather than as `slices.colors`.
 */
fun resolvePieStyleArguments(styleProperties: StylePropertiesSnapshot?): List<RenderedStyleArgument> {
    if (styleProperties == null) {
        return emptyList()
    }

    val defaultsByName = styleProperties.defaults.associate { property -> property.name to property.value }
    val membersByGroup = mutableMapOf<String, MutableList<Pair<String, KotlinLiteral>>>()
    val additionalImports = mutableSetOf<String>()

    styleProperties.current
        .sortedBy(StyleProperty::name)
        .forEach { property ->
            val (group, argumentName) = PIE_PROPERTY_GROUP[property.name] ?: return@forEach
            if (property.value == defaultsByName[property.name]) {
                return@forEach
            }
            val literal = toKotlinLiteral(propertyName = property.name, value = property.value)
            additionalImports += literal.additionalImports
            membersByGroup.getOrPut(group) { mutableListOf() } += argumentName to literal
        }

    return PIE_GROUP_ORDER.mapNotNull { group ->
        val members = membersByGroup[group] ?: return@mapNotNull null
        val memberCode = members.joinToString(" ") { (argumentName, literal) -> "$argumentName = ${literal.code}," }
        RenderedStyleArgument(
            code = "$group = ${PIE_GROUP_BUILDERS.getValue(group)}($memberCode),",
            additionalImports = additionalImports.toSet(),
        )
    }
}
