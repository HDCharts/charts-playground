package codegen

import domain.ColorValue

data class GeneratedSnippet(
    val code: String,
)

const val CODEGEN_GENERATOR_VERSION = "charts-playground-codegen-v1"

data class GeneratedArtifact(
    val source: String,
    val target: String = "kotlin-compose",
    val generatorVersion: String = CODEGEN_GENERATOR_VERSION,
    val warnings: List<String> = emptyList(),
)

interface ChartCodeGenerator<TConfig> {
    fun generate(config: TConfig): GeneratedSnippet
}

data class StylePropertiesSnapshot(
    val current: List<StyleProperty>,
    val defaults: List<StyleProperty>,
)

data class StyleProperty(
    val name: String,
    val value: StylePropertyValue,
)

sealed interface StylePropertyValue {
    data class BooleanValue(
        val value: Boolean,
    ) : StylePropertyValue

    data class ColorValue(
        val value: domain.ColorValue,
    ) : StylePropertyValue

    data class ColorListValue(
        val value: List<domain.ColorValue>,
    ) : StylePropertyValue

    data class FloatValue(
        val value: Float,
    ) : StylePropertyValue

    data class StringValue(
        val value: String,
    ) : StylePropertyValue
}

fun styleProperty(
    name: String,
    value: Boolean,
): StyleProperty = StyleProperty(name, StylePropertyValue.BooleanValue(value))

fun styleProperty(
    name: String,
    value: ColorValue,
): StyleProperty = StyleProperty(name, StylePropertyValue.ColorValue(value))

fun styleProperty(
    name: String,
    value: List<ColorValue>,
): StyleProperty = StyleProperty(name, StylePropertyValue.ColorListValue(value))

fun styleProperty(
    name: String,
    value: Float,
): StyleProperty = StyleProperty(name, StylePropertyValue.FloatValue(value))

fun styleProperty(
    name: String,
    value: String,
): StyleProperty = StyleProperty(name, StylePropertyValue.StringValue(value))
