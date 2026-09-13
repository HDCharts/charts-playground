package codegen.common

data class MultiSeriesItem(
    val label: String,
    val values: List<Float>,
)

fun buildMultiChartDataCode(
    items: List<MultiSeriesItem>,
    categories: List<String>,
): List<String> {
    val lines = mutableListOf<String>()

    lines += kotlinLine(1, "val items = listOf(")
    items.forEach { item ->
        val valuesCode = item.values.joinToString(", ") { value -> formatKotlinDoubleLiteral(value) }
        lines +=
            kotlinLine(
                2,
                "\"${escapeKotlinString(item.label)}\" to listOf($valuesCode),",
            )
    }
    lines += kotlinLine(1, ")")
    lines += ""

    val categoriesCode =
        categories.joinToString(", ") { label ->
            "\"${escapeKotlinString(label)}\""
        }

    lines += kotlinLine(1, "val data = items.toChartData(")
    lines += kotlinLine(2, "categories = listOf($categoriesCode),")
    lines += kotlinLine(1, ")")

    return lines
}
