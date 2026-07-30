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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.ChartSession
import domain.SettingChange
import domain.SettingDescriptor
import presentation.colors.toColorValue
import presentation.colors.toComposeColor
import presentation.editor.controls.BooleanToggleControl
import presentation.editor.controls.ColorPaletteControl
import presentation.editor.controls.FloatSliderControl
import presentation.editor.controls.LineColorControl
import presentation.editor.controls.StyleSectionHeader

@Composable
fun SettingsPanel(
    session: ChartSession,
    descriptors: List<SettingDescriptor>,
    onSettingChange: (SettingChange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        descriptors.forEach { descriptor ->
            when (descriptor) {
                is SettingDescriptor.Section -> StyleSectionHeader(descriptor.title)

                SettingDescriptor.Divider -> HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                is SettingDescriptor.Toggle -> {
                    val checked = descriptor.read(session.styleState) ?: descriptor.defaultValue
                    BooleanToggleControl(
                        label = descriptor.label,
                        checked = checked,
                        onCheckedChange = { nextChecked ->
                            onSettingChange(SettingChange.BooleanValue(descriptor.id, nextChecked))
                        },
                    )
                }

                is SettingDescriptor.Slider -> {
                    val value = descriptor.read(session.styleState) ?: descriptor.defaultValue
                    FloatSliderControl(
                        label = descriptor.label,
                        value = value,
                        onValueChange = { nextValue ->
                            onSettingChange(SettingChange.FloatValue(descriptor.id, nextValue))
                        },
                        valueRange = descriptor.min..descriptor.max,
                        steps = descriptor.steps,
                        formatValue = descriptor.format,
                    )
                }

                is SettingDescriptor.Dropdown -> {
                    val selected = descriptor.read(session.styleState) ?: descriptor.defaultValue
                    Text(
                        text = descriptor.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        descriptor.options.forEach { option ->
                            val isSelected = selected == option.value
                            Button(
                                onClick = {
                                    onSettingChange(SettingChange.TextValue(descriptor.id, option.value))
                                },
                                colors =
                                    if (isSelected) {
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

                is SettingDescriptor.Color -> {
                    val color = descriptor.read(session.styleState)
                    LineColorControl(
                        label = descriptor.label,
                        customColor = color?.toComposeColor(),
                        onCustomColorChange = { nextColor ->
                            onSettingChange(SettingChange.ColorValue(descriptor.id, nextColor?.toColorValue()))
                        },
                    )
                }

                is SettingDescriptor.ColorPalette -> {
                    val colors = descriptor.read(session.styleState)
                    ColorPaletteControl(
                        title = descriptor.title,
                        customColors = colors?.map { color -> color.toComposeColor() },
                        itemCount = descriptor.itemCount(session),
                        onCustomColorsChange = { nextColors ->
                            onSettingChange(
                                SettingChange.ColorListValue(
                                    descriptor.id,
                                    nextColors?.map { color -> color.toColorValue() },
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}
