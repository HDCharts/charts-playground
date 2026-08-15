package codegen.common

fun buildChartImports(
    baseImports: List<String>,
    styleImport: String? = null,
    styleArguments: List<RenderedStyleArgument> = emptyList(),
    extraImports: List<String> = emptyList(),
): List<String> {
    val imports = mutableListOf<String>()
    imports += baseImports
    styleImport?.let { imports += it }
    imports += styleArguments.flatMap { it.additionalImports }
    imports += extraImports
    return imports.distinct().sorted()
}
