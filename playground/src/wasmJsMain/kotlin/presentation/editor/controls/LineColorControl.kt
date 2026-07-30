package presentation.editor.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_style_color_content_description
import chartsproject.playground.generated.resources.playground_style_override_default
import chartsproject.playground.generated.resources.playground_style_using_chart_defaults
import org.jetbrains.compose.resources.stringResource

@Composable
fun LineColorControl(
    label: String,
    customColor: Color?,
    onCustomColorChange: (Color?) -> Unit,
) {
    BooleanToggleControl(
        label = stringResource(Res.string.playground_style_override_default),
        checked = customColor != null,
        onCheckedChange = { enabled ->
            if (enabled) {
                onCustomColorChange(customColor ?: LineColorSwatches.first())
            } else {
                onCustomColorChange(null)
            }
        },
    )

    if (customColor == null) {
        Text(
            text = stringResource(Res.string.playground_style_using_chart_defaults),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LineColorSwatches.forEachIndexed { index, color ->
            ColorSwatch(
                color = color,
                selected = color == customColor,
                onClick = { onCustomColorChange(color) },
                label =
                    stringResource(
                        Res.string.playground_style_color_content_description,
                        label,
                        index + 1,
                    ),
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
) {
    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        }

    Box(
        modifier =
            Modifier
                .width(24.dp)
                .height(24.dp)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(6.dp),
                ).background(color = color, shape = RoundedCornerShape(6.dp))
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).semantics {
                    contentDescription = label
                },
    )
}

private val LineColorSwatches: List<Color> =
    listOf(
        Color(red = 77, green = 144, blue = 254),
        Color(red = 20, green = 184, blue = 166),
        Color(red = 236, green = 72, blue = 153),
        Color(red = 234, green = 179, blue = 8),
        Color(red = 168, green = 85, blue = 247),
        Color(red = 59, green = 130, blue = 246),
        Color(red = 16, green = 185, blue = 129),
        Color(red = 244, green = 63, blue = 94),
    )
