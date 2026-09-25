package domain

import kotlin.math.roundToInt

/** Shows a setting only when it affects the chart. */
typealias SettingVisibility = (StyleResolver) -> Boolean

val AlwaysVisible: SettingVisibility = { true }

/** How a setting's value is typed in the charts library, which drives previews and generated code. */
enum class StyleKind {
    BOOLEAN,
    FLOAT,
    DP,
    INT,
    DOUBLE,
    COLOR,
    COLOR_LIST,
}

/** Where a setting's value goes. */
enum class StyleTarget {
    /** A property of the chart's library style, addressed by the setting path. */
    STYLE,

    /** Chart data rather than style, e.g. per-slice pie colors. The chart handles it itself. */
    DATA,

    /** Playground-only switch that gates the settings in its section, e.g. "Fixed Range". */
    LOCAL,
}

sealed interface SettingControl {
    data object Toggle : SettingControl

    data class Slider(
        val min: Float,
        val max: Float,
        val step: Float,
        val format: (Float) -> String,
        /** Value to show while unset when the library default is data-derived (null), e.g. an axis range. */
        val dataDefault: ((ChartData) -> Float)? = null,
        /** Bounds that follow the data, e.g. an axis range; replaces [min]..[max]. */
        val dataRange: ((ChartData) -> ClosedFloatingPointRange<Float>)? = null,
    ) : SettingControl {
        fun bounds(data: ChartData): ClosedFloatingPointRange<Float> = dataRange?.invoke(data) ?: (min..max)

        /** Tick positions for Material's Slider; data-driven sliders are continuous and snap in [snap]. */
        val steps: Int get() = if (dataRange != null) 0 else (((max - min) / step).roundToInt() - 1).coerceAtLeast(0)

        fun snap(value: Float): Float = (value / step).roundToInt() * step
    }

    data class Choice(
        val options: List<ChoiceOption>,
    ) : SettingControl

    data object ColorPick : SettingControl

    data class Palette(
        val itemCount: (ChartData) -> Int,
    ) : SettingControl
}

data class ChoiceOption(
    val label: String,
    val value: StyleValue,
)

/**
 * Reads a setting's default from the chart's library default style. Receives the library style
 * object and, for palettes, the item count. Returns a library value (Boolean, Float, Int, Double,
 * Dp, Color, or a list of Color).
 */
typealias LibraryDefault = (style: Any, itemCount: Int) -> Any?

sealed interface SettingDescriptor {
    /**
     * Starts a group of settings for one chart element. A [toggle] is shown in the header; while it
     * is off, the settings in this group are hidden because they have no effect.
     */
    data class Section(
        val title: String,
        val toggle: StyleSetting? = null,
        val visibleWhen: SettingVisibility = AlwaysVisible,
    ) : SettingDescriptor
}

/** One editable setting, declared once: its path, value kind, control, and default. */
data class StyleSetting(
    val path: String,
    val label: String,
    val kind: StyleKind,
    val control: SettingControl,
    val target: StyleTarget = StyleTarget.STYLE,
    val libraryDefault: LibraryDefault? = null,
    /** Default for [StyleTarget.LOCAL] settings, which have no library default. */
    val localDefault: StyleValue? = null,
    val visibleWhen: SettingVisibility = AlwaysVisible,
) : SettingDescriptor

/** All settings, including section toggles. */
val List<SettingDescriptor>.styleSettings: List<StyleSetting>
    get() =
        flatMap { descriptor ->
            when (descriptor) {
                is SettingDescriptor.Section -> listOfNotNull(descriptor.toggle)
                is StyleSetting -> listOf(descriptor)
            }
        }

/**
 * The settings to show: sections that apply, and the settings inside them that have an effect.
 * Settings under a section whose toggle is off are hidden.
 */
fun List<SettingDescriptor>.visibleFor(resolver: StyleResolver): List<SettingDescriptor> {
    var sectionEnabled = true
    return filter { descriptor ->
        when (descriptor) {
            is SettingDescriptor.Section -> {
                val visible = descriptor.visibleWhen(resolver)
                sectionEnabled = visible && (descriptor.toggle?.let { resolver.isOn(it.path) } ?: true)
                visible
            }
            is StyleSetting -> sectionEnabled && descriptor.visibleWhen(resolver)
        }
    }
}

/**
 * The values that reach the chart: library style values, minus those in a section gated off by a
 * playground-only toggle (e.g. a fixed axis range while "Fixed Range" is off).
 */
fun List<SettingDescriptor>.activeValues(style: ChartStyleState): ChartStyleState {
    val gatedOff = mutableSetOf<String>()
    var sectionGated = false
    forEach { descriptor ->
        when (descriptor) {
            is SettingDescriptor.Section -> {
                val toggle = descriptor.toggle
                sectionGated =
                    toggle != null &&
                    toggle.target == StyleTarget.LOCAL &&
                    ((style[toggle.path] ?: toggle.localDefault) as? StyleValue.Bool)?.value != true
            }
            is StyleSetting -> if (sectionGated) gatedOff += descriptor.path
        }
    }
    val localPaths = styleSettings.filter { it.target == StyleTarget.LOCAL }.map { it.path }.toSet()
    return ChartStyleState(style.values.filterKeys { it !in gatedOff && it !in localPaths })
}
