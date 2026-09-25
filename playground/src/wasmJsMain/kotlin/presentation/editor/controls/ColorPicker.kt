package presentation.editor.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_style_color_brightness_content_description
import chartsproject.playground.generated.resources.playground_style_color_content_description
import chartsproject.playground.generated.resources.playground_style_color_hex_label
import chartsproject.playground.generated.resources.playground_style_color_hue_content_description
import chartsproject.playground.generated.resources.playground_style_color_opacity_content_description
import org.jetbrains.compose.resources.stringResource
import presentation.colors.Hsv
import presentation.colors.isSameColorAs
import presentation.colors.parseHexColor
import presentation.colors.toHexString
import presentation.colors.toHsv

/**
 * Free-form color picker: a saturation/brightness area, a hue strip, an opacity strip,
 * a hex field, and a few quick-pick swatches. Any color, including translucent ones, can be chosen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    label: String,
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep HSV locally so hue/saturation survive when the color passes through black, white, or grey.
    var hsv by remember { mutableStateOf(color.toHsv()) }
    if (!hsv.toColor().isSameColorAs(color)) {
        hsv = color.toHsv()
    }
    val updateHsv: (Hsv) -> Unit = { next ->
        hsv = next
        onColorChange(next.toColor())
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SaturationValuePanel(
            hsv = hsv,
            onChange = updateHsv,
            contentDescription =
                stringResource(Res.string.playground_style_color_brightness_content_description, label),
        )
        HueStrip(
            hsv = hsv,
            onChange = updateHsv,
            contentDescription = stringResource(Res.string.playground_style_color_hue_content_description, label),
        )
        OpacityStrip(
            hsv = hsv,
            onChange = updateHsv,
            contentDescription = stringResource(Res.string.playground_style_color_opacity_content_description, label),
        )
        HexField(
            color = color,
            onColorChange = onColorChange,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickSwatches.forEachIndexed { index, opaqueSwatch ->
                // Swatches pick the hue only; the chosen opacity is kept.
                val swatch = opaqueSwatch.copy(alpha = color.alpha)
                ColorSwatch(
                    color = swatch,
                    selected = swatch.isSameColorAs(color),
                    onClick = { onColorChange(swatch) },
                    contentDescription =
                        stringResource(Res.string.playground_style_color_content_description, label, index + 1),
                )
            }
        }
    }
}

@Composable
private fun SaturationValuePanel(
    hsv: Hsv,
    onChange: (Hsv) -> Unit,
    contentDescription: String,
) {
    val currentHsv by rememberUpdatedState(hsv)
    val currentOnChange by rememberUpdatedState(onChange)
    val shape = RoundedCornerShape(8.dp)
    val outline = MaterialTheme.colorScheme.outline

    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(shape)
                .border(1.dp, outline.copy(alpha = 0.5f), shape)
                .semantics { this.contentDescription = contentDescription }
                .pointerInput(Unit) {
                    fun update(position: Offset) {
                        currentOnChange(
                            currentHsv.copy(
                                saturation = (position.x / size.width).coerceIn(0f, 1f),
                                value = 1f - (position.y / size.height).coerceIn(0f, 1f),
                            ),
                        )
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        update(down.position)
                        drag(down.id) { change ->
                            update(change.position)
                            change.consume()
                        }
                    }
                },
    ) {
        val pureHue = Color.hsv(hsv.hue.coerceIn(0f, 359.999f), 1f, 1f)
        drawRect(Brush.horizontalGradient(listOf(Color.White, pureHue)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        drawThumb(
            center = Offset(hsv.saturation * size.width, (1f - hsv.value) * size.height),
            fill = hsv.toColor(),
        )
    }
}

@Composable
private fun HueStrip(
    hsv: Hsv,
    onChange: (Hsv) -> Unit,
    contentDescription: String,
) {
    val currentHsv by rememberUpdatedState(hsv)
    val currentOnChange by rememberUpdatedState(onChange)
    val shape = RoundedCornerShape(8.dp)
    val outline = MaterialTheme.colorScheme.outline

    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(shape)
                .border(1.dp, outline.copy(alpha = 0.5f), shape)
                .semantics { this.contentDescription = contentDescription }
                .pointerInput(Unit) {
                    fun update(position: Offset) {
                        currentOnChange(currentHsv.copy(hue = (position.x / size.width).coerceIn(0f, 1f) * 360f))
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        update(down.position)
                        drag(down.id) { change ->
                            update(change.position)
                            change.consume()
                        }
                    }
                },
    ) {
        drawRect(Brush.horizontalGradient(HueStops))
        drawThumb(
            center = Offset(hsv.hue / 360f * size.width, size.height / 2f),
            fill = Color.hsv(hsv.hue.coerceIn(0f, 359.999f), 1f, 1f),
        )
    }
}

@Composable
private fun OpacityStrip(
    hsv: Hsv,
    onChange: (Hsv) -> Unit,
    contentDescription: String,
) {
    val currentHsv by rememberUpdatedState(hsv)
    val currentOnChange by rememberUpdatedState(onChange)
    val shape = RoundedCornerShape(8.dp)
    val outline = MaterialTheme.colorScheme.outline

    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(shape)
                .border(1.dp, outline.copy(alpha = 0.5f), shape)
                .semantics { this.contentDescription = contentDescription }
                .pointerInput(Unit) {
                    fun update(position: Offset) {
                        currentOnChange(currentHsv.copy(alpha = (position.x / size.width).coerceIn(0f, 1f)))
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        update(down.position)
                        drag(down.id) { change ->
                            update(change.position)
                            change.consume()
                        }
                    }
                },
    ) {
        drawCheckerboard()
        val opaque = hsv.copy(alpha = 1f).toColor()
        drawRect(Brush.horizontalGradient(listOf(opaque.copy(alpha = 0f), opaque)))
        drawThumb(
            center = Offset(hsv.alpha * size.width, size.height / 2f),
            fill = hsv.toColor(),
        )
    }
}

/** Grey checks behind the opacity gradient, so translucency is visible. */
private fun DrawScope.drawCheckerboard() {
    val cell = size.height / 2f
    drawRect(Color.White)
    var column = 0
    var x = 0f
    while (x < size.width) {
        for (row in 0..1) {
            if ((column + row) % 2 == 0) {
                drawRect(
                    color = Color(0xFFCCCCCC),
                    topLeft = Offset(x, row * cell),
                    size = Size(cell, cell),
                )
            }
        }
        column++
        x += cell
    }
}

@Composable
private fun HexField(
    color: Color,
    onColorChange: (Color) -> Unit,
) {
    val hex = color.toHexString()
    var text by remember { mutableStateOf(hex) }
    // The last color this field sent; only a change from elsewhere replaces what the user typed.
    var sentHex by remember { mutableStateOf(hex) }
    LaunchedEffect(hex) {
        if (hex != sentHex) {
            sentHex = hex
            text = hex
        }
    }
    val isValid = parseHexColor(text) != null
    var focused by remember { mutableStateOf(false) }

    fun send(parsed: Color) {
        sentHex = parsed.toHexString()
        onColorChange(parsed)
    }

    OutlinedTextField(
        value = text,
        onValueChange = { next ->
            text = next.take(9)
            // `#123` is also the start of `#123456`, so shorthand waits until the field loses focus.
            if (text.trim().removePrefix("#").length != 3) parseHexColor(text)?.let(::send)
        },
        label = { Text(stringResource(Res.string.playground_style_color_hex_label)) },
        singleLine = true,
        isError = !isValid,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier =
            Modifier.fillMaxWidth().onFocusChanged { focus ->
                if (focused && !focus.isFocused) {
                    parseHexColor(text)?.takeIf { it.toHexString() != sentHex }?.let(::send)
                    text = sentHex
                }
                focused = focus.isFocused
            },
    )
}

@Composable
internal fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier =
            modifier
                .size(28.dp)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        },
                    shape = shape,
                ).padding(if (selected) 3.dp else 1.dp)
                .background(color = color, shape = shape)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .semantics { this.contentDescription = contentDescription },
    )
}

private fun DrawScope.drawThumb(
    center: Offset,
    fill: Color,
) {
    val radius = 8.dp.toPx()
    drawCircle(color = fill, radius = radius, center = center)
    drawCircle(color = Color.White, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))
    drawCircle(
        color = Color.Black.copy(alpha = 0.4f),
        radius = radius + 1.dp.toPx(),
        center = center,
        style = Stroke(width = 1.dp.toPx()),
    )
}

private val HueStops: List<Color> =
    listOf(0f, 60f, 120f, 180f, 240f, 300f, 359.999f).map { hue -> Color.hsv(hue, 1f, 1f) }

private val QuickSwatches: List<Color> =
    listOf(
        Color(0xFF4D90FE),
        Color(0xFF14B8A6),
        Color(0xFF10B981),
        Color(0xFFEAB308),
        Color(0xFFF97316),
        Color(0xFFF43F5E),
        Color(0xFFEC4899),
        Color(0xFFA855F7),
        Color(0xFF6750A4),
        Color(0xFF64748B),
        Color(0xFF111827),
        Color(0xFFFFFFFF),
    )
