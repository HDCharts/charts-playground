package data

import domain.ChartData
import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.ValidationResult
import domain.formatEditorFloat
import io.github.dautovicharis.charts.demoshared.data.BarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.HistogramSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.LineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.MultiLineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.PieSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.RadarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.StackedAreaSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.StackedBarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.barSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.histogramSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.lineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.multiLineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.pieSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.radarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.stackedAreaSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.stackedBarSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet
import kotlin.math.max

internal const val LABEL_COLUMN_ID = "label"

internal object SampleDataSources {
    val pie: PieSampleUseCase = pieSampleUseCase()
    val line: LineSampleUseCase = lineSampleUseCase()
    val bar: BarSampleUseCase = barSampleUseCase()
    val histogram: HistogramSampleUseCase = histogramSampleUseCase()
    val multiLine: MultiLineSampleUseCase = multiLineSampleUseCase()
    val stackedBar: StackedBarSampleUseCase = stackedBarSampleUseCase()
    val stackedArea: StackedAreaSampleUseCase = stackedAreaSampleUseCase()
    val radar: RadarSampleUseCase = radarSampleUseCase()
}

internal fun ChartDataSet.toSingleSeries(labelsOverride: List<String>? = null): ChartData.SingleSeries {
    val labels = labelsOverride ?: data.item.labels.toList()
    return ChartData.SingleSeries(
        values = data.item.points.map(Double::toFloat),
        labels = labels.takeIf { it.isNotEmpty() },
    )
}

internal fun MultiChartDataSet.toMultiSeries(): ChartData.MultiSeries =
    ChartData.MultiSeries(
        series =
            data.items.map { item ->
                ChartData.MultiSeries.Series(
                    name = item.label,
                    values = item.item.points.map(Double::toFloat),
                )
            },
        xLabels = data.categories.toList().takeIf { it.isNotEmpty() },
    )

internal fun MultiChartDataSet.toStackedSeries(): ChartData.StackedSeries {
    val segmentNames = data.items.map { item -> item.label }
    val categoryLabels = data.categories.toList()
    val valuesPerSegment =
        data.items.map { item ->
            item.item.points.map(Double::toFloat)
        }
    val maxPoints =
        max(
            categoryLabels.size,
            valuesPerSegment.maxOfOrNull { points -> points.size } ?: 0,
        )
    val labels =
        if (maxPoints == 0) {
            emptyList()
        } else {
            List(maxPoints) { index ->
                categoryLabels.getOrNull(index) ?: "Bar ${index + 1}"
            }
        }
    val bars =
        labels.indices.map { pointIndex ->
            ChartData.StackedSeries.StackedBar(
                label = labels[pointIndex],
                values = valuesPerSegment.map { points -> points.getOrElse(pointIndex) { 0f } },
            )
        }
    return ChartData.StackedSeries(
        segmentNames = segmentNames,
        bars = bars,
        labels = labels,
    )
}

internal fun MultiChartDataSet.toRadarSeries(): ChartData.RadarSeries {
    val entries =
        data.items.map { item ->
            ChartData.RadarSeries.RadarEntry(
                name = item.label,
                values = item.item.points.map(Double::toFloat),
            )
        }
    val maxPoints = entries.maxOfOrNull { entry -> entry.values.size } ?: 0
    val axes =
        if (data.categories.isNotEmpty()) {
            data.categories.toList()
        } else {
            List(maxPoints) { index -> "Axis ${index + 1}" }
        }
    return ChartData.RadarSeries(entries = entries, axes = axes)
}

internal fun createSingleSeriesTable(
    model: ChartData.SingleSeries,
    minRows: Int,
    labelHeader: String,
): DataTableState {
    val labels = model.labels.orEmpty()
    val rowCount = max(labels.size, model.values.size)
    val columns =
        listOf(
            DataTableColumn(id = LABEL_COLUMN_ID, label = labelHeader, numeric = false, weight = 1.5f),
            DataTableColumn(id = "value", label = "Value", numeric = true, weight = 1f, defaultValue = "0"),
        )
    val rows =
        (0 until rowCount).map { index ->
            DataTableRow(
                id = index + 1,
                cells =
                    mapOf(
                        LABEL_COLUMN_ID to (labels.getOrNull(index) ?: "Item ${index + 1}"),
                        "value" to formatEditorFloat(model.values.getOrElse(index) { 0f }),
                    ),
            )
        }
    return DataTableState(columns = columns, rows = rows, minRows = minRows)
}

internal fun validateSingleSeries(
    dataTable: DataTableState,
    chartName: String,
    minRows: Int,
    labelPrefix: String,
    clampToPositive: Boolean,
): ValidationResult {
    if (dataTable.rows.size < minRows) {
        return ValidationResult(
            sanitizedTable = null,
            data = null,
            message = "$chartName needs at least $minRows rows.",
        )
    }
    val parsed =
        parseEditorTable(
            dataTable = dataTable,
            labelPrefix = labelPrefix,
            clampToPositive = clampToPositive,
        ) ?: return invalidNumericResult(dataTable)

    val valueColumn = parsed.numericColumns.firstOrNull() ?: return invalidNumericResult(dataTable)
    val values = parsed.valuesByColumn.getValue(valueColumn.id)

    return ValidationResult(
        sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
        data = ChartData.SingleSeries(values = values, labels = parsed.labels),
        message = "Applied ${parsed.labels.size} rows.",
    )
}

internal data class ParsedEditorTable(
    val labels: List<String>,
    val numericColumns: List<DataTableColumn>,
    val valuesByColumn: Map<String, List<Float>>,
    val sanitizedRows: List<DataTableRow>,
)

internal fun parseEditorTable(
    dataTable: DataTableState,
    labelPrefix: String,
    clampToPositive: Boolean,
): ParsedEditorTable? {
    val labelColumn =
        dataTable.columns.firstOrNull { column -> !column.numeric }
            ?: return null
    val numericColumns = dataTable.columns.filter { column -> column.numeric }
    if (numericColumns.isEmpty()) {
        return null
    }

    val labels = mutableListOf<String>()
    val valuesByColumn = numericColumns.associate { column -> column.id to mutableListOf<Float>() }

    dataTable.rows.forEachIndexed { index, row ->
        val label =
            row.cells[labelColumn.id]
                .orEmpty()
                .trim()
                .ifBlank { "$labelPrefix ${index + 1}" }
        labels += label

        numericColumns.forEach { column ->
            val parsedValue =
                row.cells[column.id]
                    .orEmpty()
                    .trim()
                    .toFloatOrNull() ?: return null
            valuesByColumn.getValue(column.id) += if (clampToPositive) parsedValue.coerceAtLeast(0f) else parsedValue
        }
    }

    val sanitizedRows =
        labels.indices.map { rowIndex ->
            val cells = mutableMapOf<String, String>()
            cells[labelColumn.id] = labels[rowIndex]
            numericColumns.forEach { column ->
                val value = valuesByColumn.getValue(column.id)[rowIndex]
                cells[column.id] = formatEditorFloat(value)
            }
            DataTableRow(id = dataTable.rows[rowIndex].id, cells = cells)
        }

    return ParsedEditorTable(
        labels = labels,
        numericColumns = numericColumns,
        valuesByColumn = valuesByColumn,
        sanitizedRows = sanitizedRows,
    )
}

internal fun invalidNumericResult(dataTable: DataTableState): ValidationResult =
    ValidationResult(
        sanitizedTable = null,
        data = null,
        message = "Please enter valid numeric values in all rows.",
        invalidRowIds = invalidNumericRowIds(dataTable),
    )

internal fun invalidNumericRowIds(dataTable: DataTableState): Set<Int> {
    val numericColumns = dataTable.columns.filter { column -> column.numeric }
    if (numericColumns.isEmpty()) return emptySet()
    return buildSet {
        dataTable.rows.forEachIndexed { index, row ->
            val hasInvalidNumericCell =
                numericColumns.any { column ->
                    row.cells[column.id]
                        .orEmpty()
                        .trim()
                        .toFloatOrNull() == null
                }
            if (hasInvalidNumericCell) add(index + 1)
        }
    }
}

internal fun defaultRowCells(
    columns: List<DataTableColumn>,
    rowIndex: Int,
    labelPrefix: String,
): Map<String, String> {
    val cells = mutableMapOf<String, String>()
    columns.forEach { column ->
        cells[column.id] =
            if (column.numeric) {
                column.defaultValue.ifBlank { "0" }
            } else {
                "$labelPrefix ${rowIndex + 1}"
            }
    }
    return cells
}

internal fun randomizeEditorValues(
    dataTable: DataTableState,
    valueProvider: () -> Float,
): DataTableState {
    val rows =
        dataTable.rows.map { row ->
            val cells = row.cells.toMutableMap()
            dataTable.columns.filter { column -> column.numeric }.forEach { column ->
                cells[column.id] = formatEditorFloat(valueProvider())
            }
            row.copy(cells = cells)
        }
    return dataTable.copy(rows = rows)
}
