package codegen.common

import domain.ChartData
import domain.ChartStyleState
import domain.SettingControl
import domain.SettingDescriptor
import domain.StyleKind
import domain.StyleSetting
import domain.StyleTarget
import domain.StyleValue
import domain.activeValues
import domain.normalizeColorCount
import domain.styleSettings
import kotlin.math.roundToInt

data class RenderedStyleArgument(
    val code: String,
    val additionalImports: Set<String> = emptySet(),
)

/**
 * How a chart's style is written in code. Style blocks are built with `<owner>.<block>(...)`, where
 * the owner is [styleObject] unless [blockOwners] names another, e.g. histograms build their range
 * with `BarChartDefaults`. [blockFactories] names the whole factory call for a block path, e.g.
 * `axes.grid` is built with `GridDefaults.style`.
 *
 * [blockDefaults] is keyed by block path (`range`, `axis.xLabels`) and lists members whose value in
 * the chart's default style differs from what the owner's factory builds, e.g. a histogram range
 * starts at 0 while `BarChartDefaults.range()` derives its min from the data. They are written
 * whenever their block is, unless the user set them. BlockDefaultsTest finds the members needed.
 */
data class StyleCodeProfile(
    val styleObject: String,
    val blockOwners: Map<String, String> = emptyMap(),
    val blockDefaults: Map<String, Map<String, String>> = emptyMap(),
    val blockFactories: Map<String, String> = emptyMap(),
) {
    fun owner(block: String): String = blockOwners[block] ?: styleObject

    /** The factory call that builds the block at [path], e.g. `BarChartDefaults.bars`. */
    fun factory(path: List<String>): String =
        blockFactories[path.joinToString(".")] ?: "${owner(path.first())}.${path.last()}"
}

/** Factories of the shared Cartesian `axes` block; they live in core.style. */
val cartesianAxesFactories: Map<String, String> =
    mapOf(
        "axes" to "AxesDefaults.style",
        "axes.lines" to "AxisLinesDefaults.style",
        "axes.grid" to "GridDefaults.style",
        "axes.xLabels" to "AxisLabelDefaults.style",
        "axes.yLabels" to "AxisLabelDefaults.style",
    )

/**
 * Renders the style arguments for the values the user set. Each setting path addresses the
 * library style: `a` is a direct argument, `a.b` becomes `a = Owner.a(b = …)`, and `a.b.c` becomes
 * `a = Owner.a(b = Owner.b(c = …))`. Arguments follow the settings' declaration order.
 */
fun renderStyleArguments(
    settings: List<SettingDescriptor>,
    style: ChartStyleState,
    data: ChartData,
    profile: StyleCodeProfile,
): List<RenderedStyleArgument> {
    val active = settings.activeValues(style)
    val root = StyleBlock()
    settings.styleSettings
        .filter { it.target == StyleTarget.STYLE }
        .forEach { setting ->
            val value = active[setting.path] ?: return@forEach
            root.put(setting.path.split('.'), literal(setting, value, data))
        }
    profile.blockDefaults.forEach { (blockPath, members) ->
        root.block(blockPath.split('.'))?.putDefaults(members)
    }
    return root.children.map { (name, node) ->
        val argument = renderMember(listOf(name), node, profile)
        argument.copy(code = "${argument.code},")
    }
}

private class StyleBlock {
    val children = linkedMapOf<String, Any>()

    fun put(
        segments: List<String>,
        literal: RenderedStyleArgument,
    ) {
        if (segments.size == 1) {
            children[segments.single()] = literal
        } else {
            val child = children.getOrPut(segments.first()) { StyleBlock() } as StyleBlock
            child.put(segments.drop(1), literal)
        }
    }

    fun block(segments: List<String>): StyleBlock? =
        segments.fold(this as StyleBlock?) { block, name -> block?.children?.get(name) as? StyleBlock }

    /** Adds [members] the user did not set, ahead of the ones they did. */
    fun putDefaults(members: Map<String, String>) {
        val set = LinkedHashMap(children)
        children.clear()
        members.filterKeys { it !in set }.forEach { (name, code) -> children[name] = RenderedStyleArgument(code) }
        children.putAll(set)
    }
}

/** `name = literal`, or `name = factory(member, …)` for a block. Top-level blocks end members with commas. */
private fun renderMember(
    path: List<String>,
    node: Any,
    profile: StyleCodeProfile,
): RenderedStyleArgument {
    val name = path.last()
    if (node is RenderedStyleArgument) return node.copy(code = "$name = ${node.code}")
    val members =
        (node as StyleBlock).children.map { (child, value) ->
            renderMember(path + child, value, profile)
        }
    val memberCode =
        if (path.size == 1) members.joinToString(" ") { "${it.code}," } else members.joinToString(", ") { it.code }
    val factory = profile.factory(path)
    val factoryObject = factory.substringBefore('.')
    val factoryImport = libraryImport(factoryObject).takeIf { factoryObject != profile.styleObject }
    return RenderedStyleArgument(
        code = "$name = $factory($memberCode)",
        additionalImports = members.flatMap { it.additionalImports }.toSet() + setOfNotNull(factoryImport),
    )
}

private fun literal(
    setting: StyleSetting,
    value: StyleValue,
    data: ChartData,
): RenderedStyleArgument =
    when (setting.kind) {
        StyleKind.BOOLEAN -> RenderedStyleArgument((value as StyleValue.Bool).value.toString())
        StyleKind.FLOAT -> RenderedStyleArgument(formatKotlinFloatLiteral(value.number))
        StyleKind.DP ->
            RenderedStyleArgument(
                "${formatKotlinFloatLiteral(value.number).removeSuffix("f")}.dp",
                setOf(DP_IMPORT),
            )
        StyleKind.INT -> RenderedStyleArgument(value.number.roundToInt().toString())
        StyleKind.DOUBLE -> RenderedStyleArgument(formatKotlinDoubleLiteral(value.number))
        StyleKind.COLOR -> RenderedStyleArgument(colorLiteral((value as StyleValue.Color).value), setOf(COLOR_IMPORT))
        StyleKind.COLOR_LIST -> {
            val count = (setting.control as? SettingControl.Palette)?.itemCount?.invoke(data)
            val colors =
                (value as StyleValue.Colors).value.let {
                    if (count !=
                        null
                    ) {
                        normalizeColorCount(it, count)
                    } else {
                        it
                    }
                }
            RenderedStyleArgument("listOf(${colors.joinToString(", ") { colorLiteral(it) }})", setOf(COLOR_IMPORT))
        }
    }

private val StyleValue.number: Float get() = (this as StyleValue.Number).value
