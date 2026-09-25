package presentation.editor.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_style_color_default
import chartsproject.playground.generated.resources.playground_style_color_reset
import chartsproject.playground.generated.resources.playground_style_palette_custom
import chartsproject.playground.generated.resources.playground_style_palette_hint
import chartsproject.playground.generated.resources.playground_style_palette_item_label
import org.jetbrains.compose.resources.stringResource

/**
 * One color per series/slice/bar, each picked freely.
 * The palette override is stored once the user edits an item.
 *
 * @param defaultColors The colors the chart draws with when there is no override.
 *   Editing one item keeps the others at these colors.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPaletteControl(
    title: String,
    customColors: List<Color>?,
    defaultColors: List<Color>,
    itemCount: Int,
    onCustomColorsChange: (List<Color>?) -> Unit,
) {
    val count = itemCount.coerceAtLeast(1)
    val colors = customColors?.let { repeatColors(it, count) }
    val shownColors = colors ?: repeatColors(defaultColors, count)
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val activeIndex = editingIndex?.takeIf { it < count }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text =
                    if (colors == null) {
                        stringResource(Res.string.playground_style_color_default)
                    } else {
                        stringResource(Res.string.playground_style_palette_custom)
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (colors != null) {
                TextButton(
                    onClick = {
                        editingIndex = null
                        onCustomColorsChange(null)
                    },
                ) {
                    Text(stringResource(Res.string.playground_style_color_reset))
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(count) { index ->
                val itemLabel = stringResource(Res.string.playground_style_palette_item_label, title, index + 1)
                ColorPreview(
                    color = shownColors[index],
                    selected = activeIndex == index,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .selectable(
                                selected = activeIndex == index,
                                onClick = { editingIndex = if (activeIndex == index) null else index },
                                role = Role.Tab,
                            ).semantics { contentDescription = itemLabel },
                )
            }
        }

        if (activeIndex == null) {
            Text(
                text = stringResource(Res.string.playground_style_palette_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ColorPicker(
                label = stringResource(Res.string.playground_style_palette_item_label, title, activeIndex + 1),
                color = shownColors[activeIndex],
                onColorChange = { next ->
                    onCustomColorsChange(shownColors.toMutableList().also { it[activeIndex] = next })
                },
            )
        }
    }
}

private fun repeatColors(
    baseColors: List<Color>,
    itemCount: Int,
): List<Color> {
    if (baseColors.isEmpty() || itemCount <= 0) return emptyList()
    return List(itemCount) { index -> baseColors[index % baseColors.size] }
}
