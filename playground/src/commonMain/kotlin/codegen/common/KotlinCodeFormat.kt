package codegen.common

import kotlin.math.round

private const val INDENT = "    "
private const val FLOAT_ROUNDING_SCALE = 10_000.0

fun kotlinLine(
    indentLevel: Int,
    content: String,
): String = "${INDENT.repeat(indentLevel)}$content"

fun formatKotlinFloatLiteral(value: Float): String {
    require(value.isFinite()) { "Generated Kotlin literals require finite floats: $value" }
    val rounded = round(value.toDouble() * FLOAT_ROUNDING_SCALE) / FLOAT_ROUNDING_SCALE
    val normalized = rounded.toString().removeSuffix(".0")
    return "${normalized}f"
}
