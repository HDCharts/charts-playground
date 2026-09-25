package testing

import domain.ChartData
import domain.ColorValue
import domain.SettingChange
import domain.SettingControl
import domain.SettingDescriptor
import domain.StyleTarget
import domain.StyleValue
import domain.styleSettings

private val ChangedColor = ColorValue(0xFFE63946L)

/**
 * A change for every declared setting, including section toggles. Playground-only switches are
 * turned on so the settings they gate take effect.
 */
fun List<SettingDescriptor>.changeEverySetting(data: ChartData): List<SettingChange> =
    styleSettings.map { setting ->
        val value =
            when (val control = setting.control) {
                SettingControl.Toggle -> StyleValue.Bool(setting.target == StyleTarget.LOCAL)
                is SettingControl.Slider -> StyleValue.Number(control.snap(control.bounds(data).endInclusive))
                is SettingControl.Choice -> control.options.last().value
                SettingControl.ColorPick -> StyleValue.Color(ChangedColor)
                is SettingControl.Palette -> StyleValue.Colors(List(control.itemCount(data)) { ChangedColor })
            }
        SettingChange(setting.path, value)
    }
