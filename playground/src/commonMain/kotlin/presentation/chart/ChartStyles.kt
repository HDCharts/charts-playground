package presentation.chart

import androidx.compose.runtime.Composable
import domain.ChartData
import domain.ChartType
import io.github.hdcharts.charts.style.AxisLabelStyle
import io.github.hdcharts.charts.style.BarAxisStyle
import io.github.hdcharts.charts.style.BarChartDefaults
import io.github.hdcharts.charts.style.BarChartStyle
import io.github.hdcharts.charts.style.BarGridStyle
import io.github.hdcharts.charts.style.BarRangeStyle
import io.github.hdcharts.charts.style.BarSelectionLineStyle
import io.github.hdcharts.charts.style.HistogramChartDefaults
import io.github.hdcharts.charts.style.HistogramChartStyle
import io.github.hdcharts.charts.style.LineChartDefaults
import io.github.hdcharts.charts.style.LineChartStyle
import io.github.hdcharts.charts.style.PieChartDefaults
import io.github.hdcharts.charts.style.PieChartStyle
import io.github.hdcharts.charts.style.RadarChartDefaults
import io.github.hdcharts.charts.style.RadarChartStyle
import io.github.hdcharts.charts.style.StackedAreaChartDefaults
import io.github.hdcharts.charts.style.StackedAreaChartStyle
import io.github.hdcharts.charts.style.StackedBarChartDefaults
import io.github.hdcharts.charts.style.StackedBarChartStyle

/*
 * Builds each chart's library style from the playground's settings. Every read names the setting
 * path and falls back to the library default. StyleRoundTripTest checks that each declared
 * setting is read here at its path.
 */

/** The library style for [type]; with [StyleReader.Defaults] this is the library default style. */
@Composable
internal fun chartStyle(
    type: ChartType,
    style: StyleReader,
    data: ChartData,
): Any =
    when (type) {
        ChartType.LINE, ChartType.MULTI_LINE -> lineChartStyle(style, data)
        ChartType.BAR -> barChartStyle(style, data)
        ChartType.HISTOGRAM -> histogramChartStyle(style, data)
        ChartType.STACKED_BAR -> stackedBarChartStyle(style, data)
        ChartType.AREA -> areaChartStyle(style, data)
        ChartType.RADAR -> radarChartStyle(style, data)
        ChartType.PIE -> pieChartStyle(style)
    }

@Composable
internal fun lineChartStyle(
    r: StyleReader,
    data: ChartData,
): LineChartStyle {
    val d = LineChartDefaults.style()
    val seriesCount = if (data is ChartData.MultiSeries) data.series.size else 1
    return LineChartDefaults.style(
        line =
            LineChartDefaults.line(
                color = r.color("line.color", d.line.color),
                alpha = r.float("line.alpha", d.line.alpha),
                colors = r.colors("line.colors", seriesCount, d.line.colors),
                strokeWidth = r.dp("line.strokeWidth", d.line.strokeWidth),
                bezier = r.bool("line.bezier", d.line.bezier),
            ),
        points =
            LineChartDefaults.points(
                color = r.color("points.color", d.points.color),
                size = r.dp("points.size", d.points.size),
                visible = r.bool("points.visible", d.points.visible),
            ),
        selection =
            LineChartDefaults.selection(
                color = r.color("selection.color", d.selection.color),
                size = r.dp("selection.size", d.selection.size),
                activeSize = r.dp("selection.activeSize", d.selection.activeSize),
                visible = r.bool("selection.visible", d.selection.visible),
            ),
        axis =
            LineChartDefaults.axis(
                visible = r.bool("axis.visible", d.axis.visible),
                color = r.color("axis.color", d.axis.color),
                lineWidth = r.dp("axis.lineWidth", d.axis.lineWidth),
                xLabels = r.labels("axis.xLabels", d.axis.xLabels),
                yLabels = r.labels("axis.yLabels", d.axis.yLabels),
            ),
        range =
            LineChartDefaults.range(
                min = r.double("range.min", d.range.min),
                max = r.double("range.max", d.range.max),
            ),
        legend = LineChartDefaults.legend(visible = r.bool("legend.visible", d.legend.visible)),
        zoomControlsVisible = r.bool("zoomControlsVisible", d.zoomControlsVisible),
    )
}

@Composable
internal fun barChartStyle(
    r: StyleReader,
    data: ChartData,
): BarChartStyle {
    val d = BarChartDefaults.style()
    return BarChartDefaults.style(
        bars =
            BarChartDefaults.bars(
                color = r.color("bars.color", d.bars.color),
                colors = r.colors("bars.colors", data.singleSeriesSize, d.bars.colors),
                alpha = r.float("bars.alpha", d.bars.alpha),
                space = r.dp("bars.space", d.bars.space),
                minBarWidth = r.dp("bars.minBarWidth", d.bars.minBarWidth),
            ),
        range = barRange(r, d.range),
        grid = barGrid(r, d.grid),
        axis = barAxis(r, d.axis),
        selectionLine = barSelectionLine(r, d.selectionLine),
        zoomControlsVisible = r.bool("zoomControlsVisible", d.zoomControlsVisible),
    )
}

@Composable
internal fun histogramChartStyle(
    r: StyleReader,
    data: ChartData,
): HistogramChartStyle {
    val d = HistogramChartDefaults.style()
    return HistogramChartDefaults.style(
        bars =
            HistogramChartDefaults.bars(
                color = r.color("bars.color", d.bars.color),
                colors = r.colors("bars.colors", data.singleSeriesSize, d.bars.colors),
                alpha = r.float("bars.alpha", d.bars.alpha),
                space = r.dp("bars.space", d.bars.space),
                minBarWidth = r.dp("bars.minBarWidth", d.bars.minBarWidth),
            ),
        range = barRange(r, d.range),
        grid = barGrid(r, d.grid),
        axis = barAxis(r, d.axis),
        selectionLine = barSelectionLine(r, d.selectionLine),
        zoomControlsVisible = r.bool("zoomControlsVisible", d.zoomControlsVisible),
    )
}

private fun barRange(
    r: StyleReader,
    d: BarRangeStyle,
) = BarChartDefaults.range(min = r.double("range.min", d.min), max = r.double("range.max", d.max))

@Composable
private fun barGrid(
    r: StyleReader,
    d: BarGridStyle,
) = BarChartDefaults.grid(
    visible = r.bool("grid.visible", d.visible),
    steps = r.int("grid.steps", d.steps),
    color = r.color("grid.color", d.color),
    lineWidth = d.lineWidth,
)

@Composable
private fun barAxis(
    r: StyleReader,
    d: BarAxisStyle,
) = BarChartDefaults.axis(
    visible = r.bool("axis.visible", d.visible),
    color = r.color("axis.color", d.color),
    lineWidth = d.lineWidth,
    xLabels = r.labels("axis.xLabels", d.xLabels),
    yLabels = r.labels("axis.yLabels", d.yLabels),
)

@Composable
private fun barSelectionLine(
    r: StyleReader,
    d: BarSelectionLineStyle,
) = BarChartDefaults.selectionLine(
    visible = r.bool("selectionLine.visible", d.visible),
    color = r.color("selectionLine.color", d.color),
    width = r.dp("selectionLine.width", d.width),
)

@Composable
internal fun stackedBarChartStyle(
    r: StyleReader,
    data: ChartData,
): StackedBarChartStyle {
    val d = StackedBarChartDefaults.style()
    val segmentCount = data.seriesCount
    return StackedBarChartDefaults.style(
        segments =
            StackedBarChartDefaults.segments(
                color = d.segments.color,
                colors = r.colors("segments.colors", segmentCount, d.segments.colors),
                alpha = r.float("segments.alpha", d.segments.alpha),
            ),
        layout =
            StackedBarChartDefaults.layout(
                space = r.dp("layout.space", d.layout.space),
                minBarWidth = r.dp("layout.minBarWidth", d.layout.minBarWidth),
            ),
        axis =
            StackedBarChartDefaults.axis(
                xLabels = r.labels("axis.xLabels", d.axis.xLabels),
                yLabels = r.labels("axis.yLabels", d.axis.yLabels),
            ),
        selection =
            StackedBarChartDefaults.selection(
                visible = r.bool("selection.visible", d.selection.visible),
                color = r.color("selection.color", d.selection.color),
                width = r.dp("selection.width", d.selection.width),
            ),
        zoomControlsVisible = r.bool("zoomControlsVisible", d.zoomControlsVisible),
    )
}

@Composable
internal fun areaChartStyle(
    r: StyleReader,
    data: ChartData,
): StackedAreaChartStyle {
    val d = StackedAreaChartDefaults.style()
    val seriesCount = data.seriesCount
    return StackedAreaChartDefaults.style(
        fill =
            StackedAreaChartDefaults.fill(
                color = d.fill.color,
                colors = r.colors("fill.colors", seriesCount, d.fill.colors),
                alpha = r.float("fill.alpha", d.fill.alpha),
            ),
        boundary =
            StackedAreaChartDefaults.boundary(
                visible = r.bool("boundary.visible", d.boundary.visible),
                color = d.boundary.color,
                colors = r.colors("boundary.colors", seriesCount, d.boundary.colors),
                width = r.dp("boundary.width", d.boundary.width),
                bezier = r.bool("boundary.bezier", d.boundary.bezier),
            ),
        axis =
            StackedAreaChartDefaults.axis(
                xLabels = r.labels("axis.xLabels", d.axis.xLabels),
                yLabels = r.labels("axis.yLabels", d.axis.yLabels),
            ),
        selection =
            StackedAreaChartDefaults.selection(
                visible = r.bool("selection.visible", d.selection.visible),
                color = r.color("selection.color", d.selection.color),
                width = r.dp("selection.width", d.selection.width),
            ),
        zoomControlsVisible = r.bool("zoomControlsVisible", d.zoomControlsVisible),
    )
}

@Composable
internal fun radarChartStyle(
    r: StyleReader,
    data: ChartData,
): RadarChartStyle {
    val d = RadarChartDefaults.style()
    val seriesCount = data.seriesCount
    return RadarChartDefaults.style(
        grid =
            RadarChartDefaults.grid(
                visible = r.bool("grid.visible", d.grid.visible),
                color = r.color("grid.color", d.grid.color),
                lineWidth = d.grid.lineWidth,
                steps = r.int("grid.steps", d.grid.steps),
            ),
        axes =
            RadarChartDefaults.axes(
                visible = r.bool("axes.visible", d.axes.visible),
                lineColor = r.color("axes.lineColor", d.axes.lineColor),
                lineWidth = d.axes.lineWidth,
                labelColor = r.color("axes.labelColor", d.axes.labelColor),
                labelSize = d.axes.labelSize,
                labelPadding = d.axes.labelPadding,
                labelVisible = r.bool("axes.labelVisible", d.axes.labelVisible),
            ),
        polygon =
            RadarChartDefaults.polygon(
                fillVisible = r.bool("polygon.fillVisible", d.polygon.fillVisible),
                fillAlpha = r.float("polygon.fillAlpha", d.polygon.fillAlpha),
                lineColor = d.polygon.lineColor,
                lineColors = r.colors("polygon.lineColors", seriesCount, d.polygon.lineColors),
                lineWidth = r.float("polygon.lineWidth", d.polygon.lineWidth),
            ),
        points =
            RadarChartDefaults.points(
                visible = r.bool("points.visible", d.points.visible),
                color = r.color("points.color", d.points.color),
                colorSameAsLine = r.bool("points.colorSameAsLine", d.points.colorSameAsLine),
                size = r.float("points.size", d.points.size),
            ),
        categories =
            RadarChartDefaults.categories(
                legendVisible = r.bool("categories.legendVisible", d.categories.legendVisible),
                pinsVisible = r.bool("categories.pinsVisible", d.categories.pinsVisible),
                colors = d.categories.colors,
                pinSize = d.categories.pinSize,
            ),
    )
}

/** Slice colors are applied to the pie's data rows, not its style. */
@Composable
internal fun pieChartStyle(r: StyleReader): PieChartStyle {
    val d = PieChartDefaults.style()
    return PieChartDefaults.style(
        donut = PieChartDefaults.donut(holePercentage = r.float("donut.holePercentage", d.donut.holePercentage)),
        slices =
            PieChartDefaults.slices(
                baseColor = d.slices.baseColor,
                alpha = r.float("slices.alpha", d.slices.alpha),
            ),
        border =
            PieChartDefaults.border(
                color = r.color("border.color", d.border.color),
                width = r.dp("border.width", d.border.width),
            ),
        legend = PieChartDefaults.legend(visible = r.bool("legend.visible", d.legend.visible)),
    )
}

private fun StyleReader.labels(
    path: String,
    default: AxisLabelStyle,
): AxisLabelStyle =
    default.copy(
        visible = bool("$path.visible", default.visible),
        color = color("$path.color", default.color),
        count = int("$path.count", default.count),
    )

private val ChartData.singleSeriesSize: Int get() = (this as ChartData.SingleSeries).values.size

private val ChartData.seriesCount: Int get() = (this as ChartData.MultiSeries).series.size
