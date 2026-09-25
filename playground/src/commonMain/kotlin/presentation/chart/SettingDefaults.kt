package presentation.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import domain.ChartData
import domain.ChartStyleState
import domain.ChartType
import domain.SettingControl
import domain.SettingDescriptor
import domain.StyleResolver
import domain.StyleTarget
import domain.StyleValue
import domain.styleSettings
import presentation.colors.toColorValue

/**
 * Resolves each setting to the user's value or, when unset, the chart's library default for the
 * current theme. Defaults are read from the library style through each setting's declaration, so
 * they cannot drift from the library.
 *
 * Palettes read the style with the user's settings applied, the one the preview draws: their
 * default is derived from other settings (e.g. Bar's per-bar colors repeat `bars.color`).
 */
@Composable
fun rememberSettingValues(
    type: ChartType,
    settings: List<SettingDescriptor>,
    style: ChartStyleState,
    data: ChartData,
): StyleResolver {
    val libraryStyle = chartStyle(type, StyleReader.Defaults, data)
    val activeStyle = chartStyle(type, StyleReader.active(settings, style), data)
    val defaults = mutableMapOf<String, StyleValue>()
    val resolver = StyleResolver { path -> style[path] ?: defaults[path] }
    settings.styleSettings.forEach { setting ->
        val control = setting.control
        val default =
            when {
                setting.target == StyleTarget.LOCAL -> setting.localDefault
                else -> {
                    val read = checkNotNull(setting.libraryDefault) { "'${setting.path}' declares no default" }
                    when (control) {
                        is SettingControl.Palette -> read(activeStyle, control.itemCount(data))
                        else -> read(libraryStyle, 0)
                    }?.toStyleValue()
                }
            }
        // Null is only valid where the library means "derive from the data" (an axis range); those
        // sliders declare a dataDefault to show instead. Anywhere else a null default is a gap.
        check(default != null || (control as? SettingControl.Slider)?.dataDefault != null) {
            "'${setting.path}' has no default for ${type.name}"
        }
        default?.let { defaults[setting.path] = it }
    }
    return resolver
}

/**
 * Converts a library style value (Boolean, number, Dp, Color, or list of Color) to a [StyleValue].
 * Any other type fails, so a new library value type cannot silently show no default.
 */
internal fun Any.toStyleValue(): StyleValue =
    when (this) {
        is Boolean -> StyleValue.Bool(this)
        is Dp -> StyleValue.Number(value)
        is Color -> StyleValue.Color(toColorValue())
        is Number -> StyleValue.Number(toFloat())
        is List<*> -> StyleValue.Colors(map { (it as? Color ?: unsupported(it)).toColorValue() })
        else -> unsupported(this)
    }

/** A library value type the playground does not handle yet; add a [StyleValue] and [domain.StyleKind] for it. */
private fun unsupported(value: Any?): Nothing =
    error("Unsupported library style value ${value?.let { it::class.simpleName }}: $value")
