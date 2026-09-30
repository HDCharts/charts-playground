package presentation.chart

import domain.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.model.ChartData as LibraryChartData

/*
 * The data the preview passes to the chart. Generated code writes the same data through each
 * chart's `snippetData`, so keep the two in step.
 */

internal fun ChartData.SingleSeries.toLibraryData(): LibraryChartData =
    values.map(Float::toDouble).toChartData(categories = labels.orEmpty())

internal fun ChartData.MultiSeries.toLibraryData(): LibraryChartData =
    series.map { series -> series.name to series.values.map(Float::toDouble) }.toChartData(categories = categories)
