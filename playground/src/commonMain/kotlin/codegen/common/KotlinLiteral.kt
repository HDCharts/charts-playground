package codegen.common

import codegen.StylePropertyValue
import domain.ColorValue

private const val COLOR_IMPORT = "import androidx.compose.ui.graphics.Color"

data class KotlinLiteral(
    val code: String,
    val additionalImports: Set<String> = emptySet(),
)

fun toKotlinLiteral(
    propertyName: String,
    value: StylePropertyValue,
): KotlinLiteral =
    when (value) {
        is StylePropertyValue.BooleanValue -> KotlinLiteral(code = value.value.toString())
        is StylePropertyValue.ColorValue ->
            KotlinLiteral(
                code = colorLiteral(value.value),
                additionalImports = setOf(COLOR_IMPORT),
            )
        is StylePropertyValue.ColorListValue ->
            listLiteral(propertyName = propertyName, values = value.value.map(StylePropertyValue::ColorValue))
        is StylePropertyValue.FloatValue -> KotlinLiteral(code = formatKotlinFloatLiteral(value.value))
        is StylePropertyValue.StringValue ->
            KotlinLiteral(code = "\"${escapeKotlinString(value.value)}\"")
    }

private fun listLiteral(
    propertyName: String,
    values: List<StylePropertyValue>,
): KotlinLiteral {
    val literals =
        values.mapIndexed { index, value ->
            toKotlinLiteral(
                propertyName = "$propertyName[$index]",
                value = value,
            )
        }

    return KotlinLiteral(
        code =
            if (literals.isEmpty()) {
                "listOf()"
            } else {
                "listOf(${literals.joinToString(", ") { literal -> literal.code }})"
            },
        additionalImports =
            literals
                .flatMap { it.additionalImports }
                .toSet(),
    )
}

private fun colorLiteral(color: ColorValue): String =
    "Color(0x${color.argb.toString(16).uppercase().padStart(length = 8, padChar = '0')})"
