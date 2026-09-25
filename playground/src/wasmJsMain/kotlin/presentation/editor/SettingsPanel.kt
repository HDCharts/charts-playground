package presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.ChartData
import domain.ChartSession
import domain.ChoiceOption
import domain.SettingChange
import domain.SettingControl
import domain.SettingDescriptor
import domain.StyleSetting
import domain.StyleValue
import domain.visibleFor
import presentation.chart.rememberSettingValues
import presentation.colors.toColorValue
import presentation.colors.toComposeColor
import presentation.editor.controls.BooleanToggleControl
import presentation.editor.controls.ColorControl
import presentation.editor.controls.ColorPaletteControl
import presentation.editor.controls.FloatSliderControl
import presentation.editor.controls.StyleSectionHeader

@Composable
fun SettingsPanel(
    session: ChartSession,
    descriptors: List<SettingDescriptor>,
    onSettingChange: (SettingChange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = session.validatedSpec.data
    val values = rememberSettingValues(session.chartType, descriptors, session.draft.styleState, data)

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        descriptors.visibleFor(values).forEachIndexed { index, descriptor ->
            when (descriptor) {
                is SettingDescriptor.Section ->
                    key(descriptor.title) {
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                        val toggle = descriptor.toggle
                        StyleSectionHeader(
                            title = descriptor.title,
                            checked = toggle?.let { values.isOn(it.path) },
                            onCheckedChange = { checked ->
                                toggle?.let { onSettingChange(SettingChange(it.path, StyleValue.Bool(checked))) }
                            },
                            switchContentDescription = toggle?.label ?: descriptor.title,
                        )
                    }

                is StyleSetting ->
                    // Charts share paths (Bar/Histogram `bars.color`), so control state must not carry over.
                    key(session.chartType, descriptor.path) {
                        StyleSettingControl(
                            setting = descriptor,
                            userValue = session.draft.styleState[descriptor.path],
                            effectiveValue = values.value(descriptor.path),
                            data = data,
                            onChange = { value -> onSettingChange(SettingChange(descriptor.path, value)) },
                        )
                    }
            }
        }
    }
}

/** Renders one setting with the control it declares. */
@Composable
private fun StyleSettingControl(
    setting: StyleSetting,
    userValue: StyleValue?,
    effectiveValue: StyleValue?,
    data: ChartData,
    onChange: (StyleValue?) -> Unit,
) {
    when (val control = setting.control) {
        SettingControl.Toggle ->
            BooleanToggleControl(
                label = setting.label,
                checked = setting.required<StyleValue.Bool>(effectiveValue).value,
                onCheckedChange = { checked -> onChange(StyleValue.Bool(checked)) },
            )

        is SettingControl.Slider -> {
            val bounds = control.bounds(data)
            // The library derives an unset axis range from the data, so show the data value.
            val value =
                setting.typed<StyleValue.Number>(effectiveValue)?.value
                    ?: checkNotNull(control.dataDefault) { "'${setting.path}' has no default" }.invoke(data)
            FloatSliderControl(
                label = setting.label,
                value = value.coerceIn(bounds),
                onValueChange = { next -> onChange(StyleValue.Number(control.snap(next))) },
                valueRange = bounds,
                steps = control.steps,
                formatValue = control.format,
            )
        }

        is SettingControl.Choice ->
            ChoiceControl(
                label = setting.label,
                options = control.options,
                selected = setting.required<StyleValue>(effectiveValue),
                onSelect = onChange,
            )

        SettingControl.ColorPick ->
            ColorControl(
                label = setting.label,
                customColor = setting.typed<StyleValue.Color>(userValue)?.value?.toComposeColor(),
                defaultColor = setting.required<StyleValue.Color>(effectiveValue).value.toComposeColor(),
                onCustomColorChange = { color -> onChange(color?.let { StyleValue.Color(it.toColorValue()) }) },
            )

        is SettingControl.Palette -> {
            val itemCount = control.itemCount(data)
            ColorPaletteControl(
                title = setting.label,
                customColors = setting.typed<StyleValue.Colors>(userValue)?.value?.map { it.toComposeColor() },
                defaultColors = setting.required<StyleValue.Colors>(effectiveValue).value.map { it.toComposeColor() },
                itemCount = itemCount,
                onCustomColorsChange = { colors ->
                    onChange(colors?.let { StyleValue.Colors(it.map { color -> color.toColorValue() }) })
                },
            )
        }
    }
}

/** [value] as [T]; a value of another type is a bug, so it fails instead of showing a fallback. */
private inline fun <reified T : StyleValue> StyleSetting.typed(value: StyleValue?): T? =
    value?.let { it as? T ?: error("'$path' holds $it, expected ${T::class.simpleName}") }

private inline fun <reified T : StyleValue> StyleSetting.required(value: StyleValue?): T =
    checkNotNull(typed<T>(value)) { "'$path' has no value" }

@Composable
private fun ChoiceControl(
    label: String,
    options: List<ChoiceOption>,
    selected: StyleValue?,
    onSelect: (StyleValue) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            Button(
                onClick = { onSelect(option.value) },
                colors =
                    if (selected == option.value) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                modifier = Modifier.weight(1f),
            ) {
                Text(option.label)
            }
        }
    }
}
