package data.style

import domain.AlwaysVisible
import domain.ChartData
import domain.ChoiceOption
import domain.LibraryDefault
import domain.SettingControl
import domain.SettingDescriptor
import domain.SettingVisibility
import domain.StyleKind
import domain.StyleSetting
import domain.StyleTarget
import domain.StyleValue
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Declares a chart's settings. Each setting is declared once with the path of the property in the
 * chart's library style [S]; state, defaults, visibility, and generated code all derive from it.
 */
internal fun <S : Any> styleSettings(block: StyleSettingsScope<S>.() -> Unit): List<SettingDescriptor> =
    StyleSettingsScope<S>().apply(block).build()

internal class StyleSettingsScope<S : Any> {
    private val items = mutableListOf<SettingDescriptor>()

    fun build(): List<SettingDescriptor> = items.toList()

    /** Adds a section header; its [content] settings follow it. A [toggle] appears in the header. */
    fun section(
        title: String,
        toggle: StyleSetting? = null,
        visibleWhen: SettingVisibility = AlwaysVisible,
        content: StyleSettingsScope<S>.() -> Unit = {},
    ) {
        items += SettingDescriptor.Section(title, toggle, visibleWhen)
        content()
    }

    operator fun StyleSetting.unaryPlus() {
        items += this
    }

    fun toggle(
        path: String,
        label: String,
        visibleWhen: SettingVisibility = AlwaysVisible,
        default: (S) -> Boolean,
    ): StyleSetting =
        StyleSetting(
            path = path,
            label = label,
            kind = StyleKind.BOOLEAN,
            control = SettingControl.Toggle,
            libraryDefault = library(default),
            visibleWhen = visibleWhen,
        )

    /** A playground-only switch that gates the settings in its section. */
    fun localToggle(
        path: String,
        label: String,
        default: Boolean,
    ): StyleSetting =
        StyleSetting(
            path = path,
            label = label,
            kind = StyleKind.BOOLEAN,
            control = SettingControl.Toggle,
            target = StyleTarget.LOCAL,
            localDefault = StyleValue.Bool(default),
        )

    fun slider(
        path: String,
        label: String,
        kind: StyleKind,
        range: ClosedFloatingPointRange<Float>,
        step: Float,
        format: (Float) -> String = defaultFormat(kind),
        dataDefault: ((ChartData) -> Float)? = null,
        dataRange: ((ChartData) -> ClosedFloatingPointRange<Float>)? = null,
        visibleWhen: SettingVisibility = AlwaysVisible,
        default: (S) -> Any?,
    ): StyleSetting =
        StyleSetting(
            path = path,
            label = label,
            kind = kind,
            control =
                SettingControl.Slider(
                    min = range.start,
                    max = range.endInclusive,
                    step = step,
                    format = format,
                    dataDefault = dataDefault,
                    dataRange = dataRange,
                ),
            libraryDefault = library(default),
            visibleWhen = visibleWhen,
        )

    fun choice(
        path: String,
        label: String,
        options: List<ChoiceOption>,
        kind: StyleKind = StyleKind.BOOLEAN,
        default: (S) -> Any?,
    ): StyleSetting =
        StyleSetting(
            path = path,
            label = label,
            kind = kind,
            control = SettingControl.Choice(options),
            libraryDefault = library(default),
        )

    fun color(
        path: String,
        label: String,
        visibleWhen: SettingVisibility = AlwaysVisible,
        default: (S) -> Any?,
    ): StyleSetting =
        StyleSetting(
            path = path,
            label = label,
            kind = StyleKind.COLOR,
            control = SettingControl.ColorPick,
            libraryDefault = library(default),
            visibleWhen = visibleWhen,
        )

    /**
     * One color per item. [default] returns the colors the chart draws for `itemCount` items. It
     * reads the style with the user's other settings applied, so a palette derived from a base
     * color follows that color when the user changes it.
     */
    fun palette(
        path: String,
        label: String,
        itemCount: (ChartData) -> Int,
        target: StyleTarget = StyleTarget.STYLE,
        visibleWhen: SettingVisibility = AlwaysVisible,
        default: (S, Int) -> Any?,
    ): StyleSetting =
        StyleSetting(
            path = path,
            label = label,
            kind = StyleKind.COLOR_LIST,
            control = SettingControl.Palette(itemCount),
            target = target,
            libraryDefault = { style, count ->
                @Suppress("UNCHECKED_CAST")
                default(style as S, count)
            },
            visibleWhen = visibleWhen,
        )

    private fun library(read: (S) -> Any?): LibraryDefault =
        { style, _ ->
            @Suppress("UNCHECKED_CAST")
            read(style as S)
        }
}

internal val curveOptions: List<ChoiceOption> =
    listOf(
        ChoiceOption("Bezier", StyleValue.Bool(true)),
        ChoiceOption("Linear", StyleValue.Bool(false)),
    )

/** Visible while the boolean setting at [path] is on. */
internal fun whenOn(path: String): SettingVisibility = { resolver -> resolver.isOn(path) }

internal fun whenOff(path: String): SettingVisibility = { resolver -> !resolver.isOn(path) }

internal fun whenAnyOn(vararg paths: String): SettingVisibility = { resolver -> paths.any(resolver::isOn) }

private fun defaultFormat(kind: StyleKind): (Float) -> String =
    when (kind) {
        StyleKind.INT -> { value -> value.roundToInt().toString() }
        else -> { value -> (round(value * 100) / 100.0).toString() }
    }

internal val percentFormat: (Float) -> String = { value -> "${value.toInt()}%" }
