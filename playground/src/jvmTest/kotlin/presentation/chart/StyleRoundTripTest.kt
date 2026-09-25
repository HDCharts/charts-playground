package presentation.chart

import androidx.compose.ui.graphics.Color
import data.chartCatalog
import domain.ChartStyleState
import domain.ColorValue
import domain.SettingControl
import domain.StyleKind
import domain.StyleSetting
import domain.StyleTarget
import domain.StyleValue
import domain.styleSettings
import testing.readComposable
import testing.readPath
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Every declared style setting must reach the preview: setting a value that differs from the
 * library default has to show up at the setting's path in the library style the preview builds.
 * Fails when a setting is declared but the preview style builder does not read it.
 */
class StyleRoundTripTest {
    @Test
    fun every_style_setting_reaches_the_preview_style() {
        val problems =
            chartCatalog.charts.flatMap { definition ->
                val data = definition.resetSession().validatedSpec.data
                val settings = definition.settings
                val defaultStyle = readComposable { chartStyle(definition.type, StyleReader.Defaults, data) }
                // Playground-only switches on, so the settings they gate reach the chart.
                val localSwitches =
                    settings.styleSettings
                        .filter { it.target == StyleTarget.LOCAL }
                        .associate { it.path to StyleValue.Bool(true) }

                settings.styleSettings
                    .filter { it.target == StyleTarget.STYLE }
                    .mapNotNull { setting ->
                        val default = readPath(defaultStyle, setting.path)
                        val value = valueDifferentFrom(setting, default, data)
                        val state = ChartStyleState(localSwitches + (setting.path to value))
                        val built =
                            readComposable {
                                chartStyle(definition.type, StyleReader.active(settings, state), data)
                            }
                        val actual = readPath(built, setting.path)
                        val expected = expectedLibraryValue(setting, value)
                        if (actual.normalized() == expected.normalized()) {
                            null
                        } else {
                            "${definition.type} '${setting.path}': set $expected but the preview style has $actual"
                        }
                    }
            }

        assertTrue(problems.isEmpty(), problems.joinToString(separator = "\n"))
    }
}

private val Red = ColorValue(0xFFE63946L)
private val Blue = ColorValue(0xFF1D3557L)

private fun valueDifferentFrom(
    setting: StyleSetting,
    default: Any?,
    data: domain.ChartData,
): StyleValue =
    when (val control = setting.control) {
        SettingControl.Toggle -> StyleValue.Bool(default != true)
        is SettingControl.Slider -> {
            val bounds = control.bounds(data)
            val high = control.snap(bounds.endInclusive)
            val low = control.snap(bounds.start)
            StyleValue.Number(if (default.asFloat() == high) low else high)
        }
        is SettingControl.Choice -> control.options.first { expectedChoice(it.value) != default }.value
        SettingControl.ColorPick -> StyleValue.Color(if (default == Color(Red.argb).value.toLong()) Blue else Red)
        is SettingControl.Palette -> StyleValue.Colors(List(control.itemCount(data)) { Red })
    }

private fun expectedChoice(value: StyleValue): Any =
    when (value) {
        is StyleValue.Bool -> value.value
        else -> error("Choice options of type ${value::class.simpleName} are not supported yet")
    }

private fun expectedLibraryValue(
    setting: StyleSetting,
    value: StyleValue,
): Any? =
    when (setting.kind) {
        StyleKind.BOOLEAN -> (value as StyleValue.Bool).value
        StyleKind.FLOAT, StyleKind.DP -> (value as StyleValue.Number).value
        StyleKind.INT -> (value as StyleValue.Number).value.toInt()
        StyleKind.DOUBLE -> (value as StyleValue.Number).value.toDouble()
        StyleKind.COLOR -> Color((value as StyleValue.Color).value.argb)
        StyleKind.COLOR_LIST -> (value as StyleValue.Colors).value.map { Color(it.argb) }
    }

private fun Any?.asFloat(): Float? =
    when (this) {
        is Number -> toFloat()
        else -> null
    }

/** Colors are stored unboxed as their packed value, so compare colors by that value. */
private fun Any?.normalized(): Any? =
    when (this) {
        is Float -> this
        is Double -> this
        is Int -> this
        is Color -> value.toLong()
        is List<*> -> map { it.normalized() }
        else -> this
    }
