package presentation.colors

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Hue in degrees `[0, 360)`, saturation, value, and alpha in `[0, 1]`. */
data class Hsv(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val alpha: Float = 1f,
) {
    fun toColor(): Color =
        Color.hsv(
            hue = hue.coerceIn(0f, 359.999f),
            saturation = saturation.coerceIn(0f, 1f),
            value = value.coerceIn(0f, 1f),
            alpha = alpha.coerceIn(0f, 1f),
        )
}

fun Color.toHsv(): Hsv {
    val maxChannel = max(red, max(green, blue))
    val minChannel = min(red, min(green, blue))
    val delta = maxChannel - minChannel
    val hue =
        when {
            delta == 0f -> 0f
            maxChannel == red -> 60f * (((green - blue) / delta).mod(6f))
            maxChannel == green -> 60f * ((blue - red) / delta + 2f)
            else -> 60f * ((red - green) / delta + 4f)
        }
    val saturation = if (maxChannel == 0f) 0f else delta / maxChannel
    return Hsv(hue = hue, saturation = saturation, value = maxChannel, alpha = alpha)
}

/** Formats a color as `#RRGGBB`, or as `#AARRGGBB` when it is not fully opaque. */
fun Color.toHexString(): String {
    fun channel(value: Float): String =
        (value.coerceIn(0f, 1f) * 255f)
            .roundToInt()
            .toString(16)
            .padStart(2, '0')
            .uppercase()
    val rgb = "${channel(red)}${channel(green)}${channel(blue)}"
    val alphaHex = channel(alpha)
    return if (alphaHex == "FF") "#$rgb" else "#$alphaHex$rgb"
}

/**
 * Parses `RGB`, `RRGGBB`, or `AARRGGBB`, with or without a leading `#`.
 * Returns null for anything else.
 */
fun parseHexColor(input: String): Color? {
    val hex = input.trim().removePrefix("#")
    if (hex.any { !it.isHexDigit() }) return null
    val argb =
        when (hex.length) {
            3 -> "FF" + hex.map { "$it$it" }.joinToString("")
            6 -> "FF$hex"
            8 -> hex
            else -> return null
        }
    return Color(argb.toLong(16))
}

/** True when two colors are visually identical at 8 bits per channel. */
fun Color.isSameColorAs(other: Color): Boolean = toColorValue() == other.toColorValue()

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
