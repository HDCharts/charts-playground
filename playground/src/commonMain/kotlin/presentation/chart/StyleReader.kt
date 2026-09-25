package presentation.chart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domain.ChartStyleState
import domain.SettingDescriptor
import domain.StyleValue
import domain.activeValues
import domain.normalizeColorCount
import presentation.colors.toComposeColor
import kotlin.math.roundToInt

/**
 * Reads the style values that reach the chart as library types. Each read takes the library
 * default, which is used when the user has not set the value.
 */
class StyleReader(
    private val values: ChartStyleState,
) {
    fun bool(
        path: String,
        default: Boolean,
    ): Boolean = value<StyleValue.Bool>(path)?.value ?: default

    fun float(
        path: String,
        default: Float,
    ): Float = number(path) ?: default

    fun dp(
        path: String,
        default: Dp,
    ): Dp = number(path)?.dp ?: default

    fun int(
        path: String,
        default: Int,
    ): Int = number(path)?.roundToInt() ?: default

    fun double(
        path: String,
        default: Double?,
    ): Double? = number(path)?.toDouble() ?: default

    fun color(
        path: String,
        default: Color,
    ): Color = value<StyleValue.Color>(path)?.value?.toComposeColor() ?: default

    /** One color per item, repeating the user's colors when there are more items than colors. */
    fun colors(
        path: String,
        itemCount: Int,
        default: List<Color>,
    ): List<Color> =
        value<StyleValue.Colors>(path)
            ?.value
            ?.let { normalizeColorCount(it, itemCount).map { color -> color.toComposeColor() } }
            ?: default

    private fun number(path: String): Float? = value<StyleValue.Number>(path)?.value

    /** The user's value at [path]; a value of another type is a bug, not a reason to use the default. */
    private inline fun <reified T : StyleValue> value(path: String): T? =
        values[path]?.let { it as? T ?: error("'$path' holds $it, expected ${T::class.simpleName}") }

    companion object {
        /** Reads only library defaults. */
        val Defaults = StyleReader(ChartStyleState())

        fun active(
            settings: List<SettingDescriptor>,
            style: ChartStyleState,
        ): StyleReader = StyleReader(settings.activeValues(style))
    }
}
