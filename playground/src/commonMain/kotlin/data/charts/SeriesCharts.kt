package data.charts

import codegen.SnippetData
import data.PlaygroundChart
import data.createMultiSeriesTable
import data.createSingleSeriesTable
import data.defaultRowCells
import data.randomizeEditorValues
import data.validateMultiSeries
import data.validateSingleSeries
import domain.ChartData
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableState
import domain.ValidatedChartSpec
import domain.ValidationResult
import kotlin.random.Random

/**
 * The editor side of a chart: how its data table is built, validated, extended, and randomized.
 *
 * @param labelPrefix Names rows whose label is left blank, e.g. `Bar 3`.
 * @param nonNegative Whether negative values are rejected.
 * @param randomValues The range "Randomize" draws values from.
 */
internal abstract class TableChart(
    final override val type: ChartType,
    final override val defaultTitle: String,
    protected val labelPrefix: String,
    protected val labelHeader: String,
    protected val minRows: Int,
    protected val nonNegative: Boolean,
    private val randomValues: ClosedFloatingPointRange<Float>,
) : PlaygroundChart {
    final override val displayName: String get() = type.displayName

    final override fun newRowCells(
        rowIndex: Int,
        columns: List<DataTableColumn>,
    ): Map<String, String> = defaultRowCells(columns, rowIndex, labelPrefix)

    final override fun randomize(dataTable: DataTableState): DataTableState =
        randomizeEditorValues(dataTable) {
            randomValues.start + Random.nextFloat() * (randomValues.endInclusive - randomValues.start)
        }
}

/** A chart with one value per label, edited as a label column and a value column. */
internal abstract class SingleSeriesChart(
    type: ChartType,
    defaultTitle: String,
    labelPrefix: String,
    randomValues: ClosedFloatingPointRange<Float>,
    nonNegative: Boolean = false,
    labelHeader: String = "Label",
) : TableChart(type, defaultTitle, labelPrefix, labelHeader, minRows = 2, nonNegative, randomValues) {
    final override fun createDataTable(chartData: ChartData): DataTableState =
        createSingleSeriesTable(chartData as ChartData.SingleSeries, minRows, labelHeader, labelPrefix)

    final override fun validate(dataTable: DataTableState): ValidationResult =
        validateSingleSeries(dataTable, type, minRows, labelPrefix, nonNegative)

    override fun snippetData(spec: ValidatedChartSpec): SnippetData {
        val data = spec.data as ChartData.SingleSeries
        return SnippetData.Values(values = data.values, categories = data.labels.orEmpty())
    }
}

/** Named series over shared categories, edited as one row per category and a column per series. */
internal abstract class MultiSeriesChart(
    type: ChartType,
    defaultTitle: String,
    labelPrefix: String,
    randomValues: ClosedFloatingPointRange<Float>,
    nonNegative: Boolean = false,
    labelHeader: String = "Category",
    minRows: Int = 2,
) : TableChart(type, defaultTitle, labelPrefix, labelHeader, minRows, nonNegative, randomValues) {
    final override fun createDataTable(chartData: ChartData): DataTableState =
        createMultiSeriesTable(chartData as ChartData.MultiSeries, minRows, labelHeader)

    final override fun validate(dataTable: DataTableState): ValidationResult =
        validateMultiSeries(dataTable, type, minRows, labelPrefix, nonNegative)

    final override fun snippetData(spec: ValidatedChartSpec): SnippetData {
        val data = spec.data as ChartData.MultiSeries
        return SnippetData.Series(series = data.series.map { it.name to it.values }, categories = data.categories)
    }
}
