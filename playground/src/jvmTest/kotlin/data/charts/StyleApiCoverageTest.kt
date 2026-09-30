package data.charts

import data.chartCatalog
import domain.ChartType
import domain.StyleTarget
import domain.styleSettings
import io.github.hdcharts.core.style.BarChartStyle
import io.github.hdcharts.core.style.HistogramChartStyle
import io.github.hdcharts.line.LineChartStyle
import io.github.hdcharts.pie.PieChartStyle
import io.github.hdcharts.radar.RadarChartStyle
import io.github.hdcharts.stackedarea.StackedAreaChartStyle
import io.github.hdcharts.stackedbar.StackedBarChartStyle
import java.io.File
import java.lang.reflect.Modifier
import java.net.JarURLConnection
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Keeps the playground in step with the charts library's style API.
 *
 * Every public style property of every chart must be covered by a declared setting, or listed
 * here as not applicable (with a reason) or not exposed yet. When the library adds, renames, or
 * removes a style property, this test names it so the playground gets updated deliberately.
 */
class StyleApiCoverageTest {
    @Test
    fun every_library_style_property_is_accounted_for() {
        val problems =
            chartCatalog.charts.flatMap { definition ->
                val coverage = coverageFor(definition.type)
                val covered =
                    definition.settings.styleSettings
                        .filter { it.target == StyleTarget.STYLE }
                        .map { it.path }
                val library = styleProperties(coverage.styleClass)
                val listed = covered + coverage.notApplicable.keys + coverage.notExposedYet
                buildList {
                    (library - listed.toSet()).sorted().forEach { path ->
                        add("${definition.type}: library property '$path' needs a setting or a not-exposed entry")
                    }
                    (listed.toSet() - library).sorted().forEach { path ->
                        add("${definition.type}: '$path' is declared or listed but does not exist in the library")
                    }
                    listed.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.sorted().forEach { path ->
                        add("${definition.type}: '$path' is covered or listed more than once")
                    }
                }
            }

        assertTrue(problems.isEmpty(), problems.joinToString(separator = "\n"))
    }

    @Test
    fun every_library_chart_has_a_playground_chart() {
        val covered = chartCatalog.charts.map { coverageFor(it.type).styleClass.simpleName }.toSet()
        val missing = libraryChartStyles() - covered

        assertTrue(
            missing.isEmpty(),
            "The charts library has charts the playground does not offer yet: ${missing.sorted()}",
        )
    }
}

/** Every `*ChartStyle` class the charts library ships, across all of its modules on the classpath. */
private fun libraryChartStyles(): Set<String> {
    val directory = LIBRARY_PACKAGE.replace('.', '/')
    val entries =
        Thread.currentThread().contextClassLoader.getResources(directory).toList().flatMap { url ->
            when (url.protocol) {
                "jar" ->
                    (url.openConnection() as JarURLConnection).jarFile.use { jar ->
                        jar
                            .entries()
                            .toList()
                            .map { it.name }
                            .filter { it.startsWith("$directory/") }
                    }
                "file" ->
                    File(
                        url.toURI(),
                    ).walk().map { "$directory/${it.relativeTo(File(url.toURI())).path}" }.toList()
                else -> error("Cannot list library classes from $url")
            }
        }
    return entries
        .filterNot { "/internal/" in it }
        .map { it.substringAfterLast('/') }
        .filter { it.endsWith("ChartStyle.class") && '$' !in it && it != "ChartStyle.class" }
        .map { it.removeSuffix(".class") }
        .toSet()
        .also { check(it.isNotEmpty()) { "Found no library chart styles under $directory" } }
}

private class StyleCoverage(
    val styleClass: Class<*>,
    val notApplicable: Map<String, String> = emptyMap(),
    val notExposedYet: Set<String> = emptySet(),
)

private val pageLayout =
    listOf("chartContainerStyle.contentPadding", "chartContainerStyle.styleTitle")
        .associateWith { "Title text style and padding belong to the page layout, not chart styling" }

private val labelCounts =
    listOf("axis.xLabels.maxCount", "axis.yLabels.maxCount")
        .associateWith { "The chart picks how many labels fit; the playground keeps that default" }

private val labelSizes = setOf("axis.xLabels.size", "axis.yLabels.size")

private fun coverageFor(chartType: ChartType): StyleCoverage =
    when (chartType) {
        ChartType.LINE ->
            StyleCoverage(
                styleClass = LineChartStyle::class.java,
                notApplicable =
                    pageLayout + labelCounts +
                        mapOf(
                            "line.colors" to "A single series draws with line.color",
                            "legend.visible" to "The legend is only drawn for multiple series",
                        ),
                notExposedYet = labelSizes,
            )

        ChartType.MULTI_LINE ->
            StyleCoverage(
                styleClass = LineChartStyle::class.java,
                notApplicable = pageLayout + labelCounts + ("line.color" to "Per-series colors are edited directly"),
                notExposedYet = labelSizes,
            )

        ChartType.BAR -> barCoverage(BarChartStyle::class.java)

        ChartType.HISTOGRAM -> barCoverage(HistogramChartStyle::class.java)

        ChartType.STACKED_BAR ->
            StyleCoverage(
                styleClass = StackedBarChartStyle::class.java,
                notApplicable =
                    pageLayout + labelCounts + ("segments.color" to "Per-segment colors are edited directly"),
                notExposedYet = labelSizes,
            )

        ChartType.AREA ->
            StyleCoverage(
                styleClass = StackedAreaChartStyle::class.java,
                notApplicable =
                    pageLayout + labelCounts +
                        mapOf(
                            "fill.color" to "Per-series fill colors are edited directly",
                            "boundary.color" to "Per-series line colors are edited directly",
                        ),
                notExposedYet = labelSizes,
            )

        ChartType.RADAR ->
            StyleCoverage(
                styleClass = RadarChartStyle::class.java,
                notApplicable = pageLayout + ("polygon.lineColor" to "Per-series line colors are edited directly"),
                notExposedYet =
                    setOf(
                        "grid.lineWidth",
                        "axes.lineWidth",
                        "axes.labelSize",
                        "axes.labelPadding",
                        "categories.colors",
                        "categories.pinSize",
                    ),
            )

        ChartType.PIE ->
            StyleCoverage(
                styleClass = PieChartStyle::class.java,
                notApplicable = pageLayout + ("slices.baseColor" to "Per-slice colors are edited directly"),
            )
    }

private fun barCoverage(styleClass: Class<*>): StyleCoverage =
    StyleCoverage(
        styleClass = styleClass,
        notApplicable = pageLayout + labelCounts,
        notExposedYet = labelSizes + setOf("grid.lineWidth", "axis.lineWidth"),
    )

private const val LIBRARY_PACKAGE = "io.github.hdcharts"

/** Public style properties of [styleClass] as dotted paths, descending into nested style blocks. */
private fun styleProperties(
    styleClass: Class<*>,
    prefix: String = "",
): Set<String> =
    styleClass.declaredFields
        .filterNot { field -> Modifier.isStatic(field.modifiers) || field.isSynthetic }
        .filter { field -> styleClass.hasPublicGetter(field.name) }
        .flatMap { field ->
            val path = prefix + field.name
            // Nested style blocks (BarGridStyle, LegendStyle, …) live in core.style or the chart's own package.
            if (field.type.packageName.startsWith("$LIBRARY_PACKAGE.") && field.type.simpleName.endsWith("Style")) {
                styleProperties(field.type, "$path.")
            } else {
                setOf(path)
            }
        }.toSet()

private fun Class<*>.hasPublicGetter(property: String): Boolean {
    val getter = "get" + property.replaceFirstChar(Char::uppercase)
    // Getters of value-class properties (Color, Dp, TextUnit) carry a mangled suffix, e.g. getColor-0d7_KjU.
    return methods.any { method ->
        method.parameterCount == 0 && (method.name == getter || method.name.startsWith("$getter-"))
    }
}
