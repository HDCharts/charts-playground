package codegen.common

/**
 * Renders the Kotlin fragments shared by chart-specific code generators.
 *
 * Chart generators compose this renderer instead of inheriting implementation
 * details from a common base class.
 */
internal class ChartCodeRenderer {
    fun renderFunction(
        imports: List<String>,
        functionName: String,
        bodyLines: List<String>,
    ): String =
        buildString {
            append(imports.joinToString("\n"))
            append("\n\n")
            append("@Composable\n")
            append("fun $functionName() {\n")
            append(bodyLines.joinToString("\n"))
            append("\n}")
        }

    fun renderDataSet(
        items: List<NormalizedPoint>,
        title: String,
    ): List<String> {
        val valuesCode = items.joinToString(", ") { item -> formatKotlinFloatLiteral(item.value) }
        val labelsCode = items.joinToString(", ") { item -> "\"${escapeKotlinString(item.label)}\"" }

        return buildList {
            add(kotlinLine(1, "val dataSet ="))
            add(kotlinLine(2, "listOf($valuesCode).toChartDataSet("))
            add(kotlinLine(3, "title = \"${escapeKotlinString(title)}\","))
            add(kotlinLine(3, "labels = listOf($labelsCode),"))
            add(kotlinLine(2, ")"))
        }
    }

    fun renderStyle(
        styleBuilder: String,
        styleArguments: List<RenderedStyleArgument>,
    ): List<String> =
        buildList {
            add(kotlinLine(1, "val style ="))
            add(kotlinLine(2, "$styleBuilder("))
            styleArguments.forEach { argument -> add(kotlinLine(3, argument.code)) }
            add(kotlinLine(2, ")"))
        }

    fun renderChartCall(
        componentName: String,
        includeStyle: Boolean,
    ): List<String> =
        if (includeStyle) {
            listOf(
                kotlinLine(1, "$componentName("),
                kotlinLine(2, "dataSet = dataSet,"),
                kotlinLine(2, "style = style,"),
                kotlinLine(1, ")"),
            )
        } else {
            listOf(kotlinLine(1, "$componentName(dataSet = dataSet)"))
        }
}

internal data class NormalizedPoint(
    val label: String,
    val value: Float,
)
