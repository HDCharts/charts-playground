package codegen.common

/** Package names and imports of the charts library and Compose, as generated code writes them. */
internal const val COLOR_IMPORT = "import androidx.compose.ui.graphics.Color"
internal const val DP_IMPORT = "import androidx.compose.ui.unit.dp"

/** Package of each library symbol generated code imports; each chart module has its own package. */
private val libraryPackages: Map<String, String> =
    mapOf(
        "toChartData" to "core.model",
        "BarChartDefaults" to "core.style",
        "HistogramChartDefaults" to "core.style",
        "BarChart" to "bar",
        "HistogramChart" to "histogram",
        "LineChart" to "line",
        "LineChartDefaults" to "line",
        "PieChart" to "pie",
        "PieChartDefaults" to "pie",
        "RadarChart" to "radar",
        "RadarChartDefaults" to "radar",
        "StackedAreaChart" to "stackedarea",
        "StackedAreaChartDefaults" to "stackedarea",
        "StackedBarChart" to "stackedbar",
        "StackedBarChartDefaults" to "stackedbar",
    ).mapValues { (_, suffix) -> "io.github.hdcharts.$suffix" }

/** The package that declares library [symbol], e.g. `io.github.hdcharts.bar` for `BarChart`. */
internal fun libraryPackage(symbol: String): String =
    libraryPackages[symbol] ?: error("No library package known for '$symbol'")

/** The import line for library [symbol]. */
internal fun libraryImport(symbol: String): String = "import ${libraryPackage(symbol)}.$symbol"
