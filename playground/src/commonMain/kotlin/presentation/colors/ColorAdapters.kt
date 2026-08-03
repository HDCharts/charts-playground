package presentation.colors

import androidx.compose.ui.graphics.Color
import domain.ColorValue
import kotlin.math.roundToInt

internal fun Color.toColorValue(): ColorValue =
    ColorValue(
        argb =
            (channelToByte(alpha).toLong() shl 24) or
                (channelToByte(red).toLong() shl 16) or
                (channelToByte(green).toLong() shl 8) or
                channelToByte(blue).toLong(),
    )

internal fun ColorValue.toComposeColor(): Color = Color(argb)

private fun channelToByte(channel: Float): Int = (channel.coerceIn(0f, 1f) * 255f).roundToInt()
