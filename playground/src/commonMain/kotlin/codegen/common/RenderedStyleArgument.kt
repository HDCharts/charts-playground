package codegen.common

import codegen.StylePropertiesSnapshot
import codegen.StyleProperty

data class RenderedStyleArgument(
    val code: String,
    val additionalImports: Set<String> = emptySet(),
)

fun resolveStyleArguments(styleProperties: StylePropertiesSnapshot?): List<RenderedStyleArgument> {
    if (styleProperties == null) {
        return emptyList()
    }

    val defaultsByName = styleProperties.defaults.associate { property -> property.name to property.value }
    return styleProperties.current.sortedBy(StyleProperty::name).mapNotNull { property ->
        val name = property.name
        val currentValue = property.value
        val defaultValue = defaultsByName[name]
        if (currentValue == defaultValue) {
            null
        } else {
            val literal = toKotlinLiteral(propertyName = name, value = currentValue)
            RenderedStyleArgument(
                code = "$name = ${literal.code},",
                additionalImports = literal.additionalImports,
            )
        }
    }
}
