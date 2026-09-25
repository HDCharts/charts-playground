package presentation.editor.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_style_color_default
import chartsproject.playground.generated.resources.playground_style_color_edit_content_description
import chartsproject.playground.generated.resources.playground_style_color_reset
import org.jetbrains.compose.resources.stringResource
import presentation.colors.toHexString

/**
 * A single color setting. Shows the current color (or "Default"), and expands into a
 * free-form [ColorPicker]. The override is only stored once the user picks a color.
 *
 * @param defaultColor The color the chart draws with when there is no override.
 */
@Composable
fun ColorControl(
    label: String,
    customColor: Color?,
    defaultColor: Color,
    onCustomColorChange: (Color?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val valueText = customColor?.toHexString() ?: stringResource(Res.string.playground_style_color_default)
    val editDescription = stringResource(Res.string.playground_style_color_edit_content_description, label)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .semantics(mergeDescendants = true) {
                        contentDescription = editDescription
                        stateDescription = valueText
                    }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ColorPreview(color = customColor ?: defaultColor, selected = expanded)
        }

        if (expanded) {
            ColorPicker(
                label = label,
                color = customColor ?: defaultColor,
                onColorChange = { next -> onCustomColorChange(next) },
            )
            if (customColor != null) {
                TextButton(
                    onClick = { onCustomColorChange(null) },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(Res.string.playground_style_color_reset))
                }
            }
        }
    }
}

/** A color chip. */
@Composable
internal fun ColorPreview(
    color: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    Box(
        modifier =
            modifier
                .size(28.dp)
                .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = shape)
                .padding(if (selected) 3.dp else 1.dp)
                .clip(shape),
    ) {
        Box(Modifier.fillMaxSize().background(color))
    }
}
