package domain

/** A value the user set for one style setting. The setting's [StyleKind] says how to interpret it. */
sealed interface StyleValue {
    data class Bool(
        val value: Boolean,
    ) : StyleValue

    data class Number(
        val value: Float,
    ) : StyleValue

    data class Color(
        val value: ColorValue,
    ) : StyleValue

    data class Colors(
        val value: List<ColorValue>,
    ) : StyleValue
}

/**
 * The style values the user has set, keyed by setting path (for library settings, the path of the
 * property in the chart's library style, e.g. `points.size`). A missing path means the library
 * default applies.
 */
data class ChartStyleState(
    val values: Map<String, StyleValue> = emptyMap(),
) {
    operator fun get(path: String): StyleValue? = values[path]

    fun with(
        path: String,
        value: StyleValue?,
    ): ChartStyleState = copy(values = if (value == null) values - path else values + (path to value))
}

/** Looks up the effective value of a setting: the user's value, or its default. */
fun interface StyleResolver {
    fun value(path: String): StyleValue?

    fun isOn(path: String): Boolean = (value(path) as? StyleValue.Bool)?.value == true
}

/** Per-slice pie colors, which the pie chart takes on its data rows rather than its style. */
const val PIE_SLICE_COLORS_PATH = "data.sliceColors"
