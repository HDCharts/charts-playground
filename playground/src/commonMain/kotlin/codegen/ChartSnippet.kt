package codegen

import codegen.common.CHARTS_PACKAGE
import codegen.common.COLOR_IMPORT
import codegen.common.RenderedStyleArgument
import codegen.common.STYLE_PACKAGE
import codegen.common.colorLiteral
import codegen.common.escapeKotlinString
import codegen.common.formatKotlinDoubleLiteral
import codegen.common.kotlinLine
import domain.ColorValue

/** A generated usage example: one chart composable with its data and, when any is set, its style. */
data class ChartSnippet(
    /** The chart composable, e.g. `BarChart`. */
    val component: String,
    /** The chart's defaults object that builds its style, e.g. `BarChartDefaults`. */
    val styleObject: String,
    val data: SnippetData,
    val title: String,
    val functionName: String,
    val styleArguments: List<RenderedStyleArgument> = emptyList(),
)

/** Chart data as generated code writes it; it mirrors what the preview passes to the chart. */
sealed interface SnippetData {
    /** One value per category: `listOf(…).toChartData(categories = …)`. */
    data class Values(
        val values: List<Float>,
        val categories: List<String>,
    ) : SnippetData

    /** Named series over shared categories: `listOf("name" to listOf(…)).toChartData(categories = …)`. */
    data class Series(
        val series: List<Pair<String, List<Float>>>,
        val categories: List<String>,
    ) : SnippetData

    /** Pie slices; a slice color, when set, goes on its data row. */
    data class Slices(
        val slices: List<Slice>,
    ) : SnippetData {
        data class Slice(
            val label: String,
            val value: Float,
            val color: ColorValue? = null,
        )
    }
}

fun ChartSnippet.render(): String {
    val includeStyle = styleArguments.isNotEmpty()
    val imports =
        buildList {
            add("import androidx.compose.runtime.Composable")
            add("import $CHARTS_PACKAGE.$component")
            addAll(data.imports())
            if (includeStyle) add("import $STYLE_PACKAGE.$styleObject")
            addAll(styleArguments.flatMap { it.additionalImports })
        }.distinct().sorted()
    val body =
        buildList {
            addAll(data.render())
            add("")
            if (includeStyle) {
                add(kotlinLine(1, "val style ="))
                add(kotlinLine(2, "$styleObject.style("))
                styleArguments.forEach { argument -> add(kotlinLine(3, argument.code)) }
                add(kotlinLine(2, ")"))
                add("")
            }
            addAll(chartCall(includeStyle))
        }
    return buildString {
        append(imports.joinToString("\n"))
        append("\n\n")
        append("@Composable\n")
        append("fun $functionName() {\n")
        append(body.joinToString("\n"))
        append("\n}\n")
    }
}

private fun ChartSnippet.chartCall(includeStyle: Boolean): List<String> {
    val titleCode = "\"${escapeKotlinString(title)}\""
    return if (includeStyle) {
        listOf(
            kotlinLine(1, "$component("),
            kotlinLine(2, "data = data,"),
            kotlinLine(2, "title = $titleCode,"),
            kotlinLine(2, "style = style,"),
            kotlinLine(1, ")"),
        )
    } else {
        listOf(kotlinLine(1, "$component(data = data, title = $titleCode)"))
    }
}

private fun SnippetData.imports(): List<String> =
    when (this) {
        is SnippetData.Values, is SnippetData.Series -> listOf("import $CHARTS_PACKAGE.model.toChartData")
        is SnippetData.Slices ->
            listOfNotNull(
                "import androidx.compose.runtime.remember",
                "import $CHARTS_PACKAGE.model.PieSlice",
                COLOR_IMPORT.takeIf { slices.any { it.color != null } },
            )
    }

private fun SnippetData.render(): List<String> =
    when (this) {
        is SnippetData.Values ->
            listOf(
                kotlinLine(1, "val data ="),
                kotlinLine(
                    2,
                    "listOf(${values.joinToString(", ", transform = ::formatKotlinDoubleLiteral)}).toChartData(",
                ),
                kotlinLine(3, "categories = ${stringList(categories)},"),
                kotlinLine(2, ")"),
            )
        is SnippetData.Series ->
            buildList {
                add(kotlinLine(1, "val items = listOf("))
                series.forEach { (name, values) ->
                    val valuesCode = values.joinToString(", ", transform = ::formatKotlinDoubleLiteral)
                    add(kotlinLine(2, "${stringLiteral(name)} to listOf($valuesCode),"))
                }
                add(kotlinLine(1, ")"))
                add("")
                add(kotlinLine(1, "val data = items.toChartData("))
                add(kotlinLine(2, "categories = ${stringList(categories)},"))
                add(kotlinLine(1, ")"))
            }
        is SnippetData.Slices ->
            buildList {
                add(kotlinLine(1, "val data ="))
                add(kotlinLine(1, "remember {"))
                add(kotlinLine(2, "listOf("))
                slices.forEach { slice ->
                    val args =
                        listOfNotNull(
                            "label = ${stringLiteral(slice.label)}",
                            "value = ${formatKotlinDoubleLiteral(slice.value)}",
                            slice.color?.let { "color = ${colorLiteral(it)}" },
                        )
                    add(kotlinLine(3, "PieSlice(${args.joinToString(", ")}),"))
                }
                add(kotlinLine(2, ")"))
                add(kotlinLine(1, "}"))
            }
    }

private fun stringLiteral(value: String): String = "\"${escapeKotlinString(value)}\""

private fun stringList(values: List<String>): String =
    "listOf(${values.joinToString(", ", transform = ::stringLiteral)})"
