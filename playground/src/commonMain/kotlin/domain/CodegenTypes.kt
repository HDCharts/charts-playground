package domain

fun deriveFunctionName(
    title: String,
    chartType: ChartType,
): String {
    val parts = "[A-Za-z0-9]+".toRegex().findAll(title).map { it.value }.toList()
    val base = parts.joinToString(separator = "") { part -> part.replaceFirstChar { it.uppercase() } }
    val fallback = "Sample${chartType.codegenSuffix}"
    val withSuffix = if (base.isBlank()) fallback else "${base}${chartType.codegenSuffix}"
    return if (withSuffix.firstOrNull()?.isDigit() == true) "Sample$withSuffix" else withSuffix
}
