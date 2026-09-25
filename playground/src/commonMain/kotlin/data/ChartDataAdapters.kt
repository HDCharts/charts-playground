package data

import domain.ChartData
import domain.ChartType
import domain.DataTableColumn
import domain.DataTableRow
import domain.DataTableState
import domain.RowId
import domain.ValidationArgument
import domain.ValidationIssue
import domain.ValidationIssueCode
import domain.ValidationPath
import domain.ValidationResult
import domain.ValidationSeverity
import domain.formatEditorFloat
import domain.sortedDeterministically
import io.github.hdcharts.charts.model.PieSlice
import io.github.hdcharts.sampleshared.data.BarSampleUseCase
import io.github.hdcharts.sampleshared.data.HistogramSampleUseCase
import io.github.hdcharts.sampleshared.data.LineSampleUseCase
import io.github.hdcharts.sampleshared.data.MultiLineSampleUseCase
import io.github.hdcharts.sampleshared.data.PieSampleUseCase
import io.github.hdcharts.sampleshared.data.RadarSampleUseCase
import io.github.hdcharts.sampleshared.data.StackedAreaSampleUseCase
import io.github.hdcharts.sampleshared.data.StackedBarSampleUseCase
import io.github.hdcharts.sampleshared.data.barSampleUseCase
import io.github.hdcharts.sampleshared.data.histogramSampleUseCase
import io.github.hdcharts.sampleshared.data.lineSampleUseCase
import io.github.hdcharts.sampleshared.data.multiLineSampleUseCase
import io.github.hdcharts.sampleshared.data.pieSampleUseCase
import io.github.hdcharts.sampleshared.data.radarSampleUseCase
import io.github.hdcharts.sampleshared.data.stackedAreaSampleUseCase
import io.github.hdcharts.sampleshared.data.stackedBarSampleUseCase
import kotlin.math.max
import kotlin.math.roundToInt
import io.github.hdcharts.charts.model.ChartData as LibraryChartData

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

internal fun LibraryChartData.toSingleSeries(labelsOverride: List<String>? = null): ChartData.SingleSeries {
    val series = series.single()
    val labels = labelsOverride ?: categories.toList()
    return ChartData.SingleSeries(
        values = series.values.map(Double::toFloat),
        labels = labels.takeIf { it.isNotEmpty() },
    )
}

internal fun List<PieSlice>.toSingleSeries(): ChartData.SingleSeries =
    ChartData.SingleSeries(
        values = map { it.value.toFloat() },
        labels = map { it.label }.takeIf { it.isNotEmpty() },
    )

/**
 * Library data as named series over shared categories. Missing categories are named with
 * [labelPrefix] and missing values are 0, so every series has one value per category.
 */
internal fun LibraryChartData.toMultiSeries(labelPrefix: String): ChartData.MultiSeries {
    val size = max(categories.size, series.maxOfOrNull { item -> item.values.size } ?: 0)
    return ChartData.MultiSeries(
        categories = List(size) { index -> categories.getOrNull(index) ?: "$labelPrefix ${index + 1}" },
        series =
            series.map { item ->
                ChartData.MultiSeries.Series(
                    name = item.name.orEmpty(),
                    values = List(size) { index -> item.values.getOrNull(index)?.toFloat() ?: 0f },
                )
            },
    )
}

internal fun createSingleSeriesTable(
    model: ChartData.SingleSeries,
    minRows: Int,
    labelHeader: String,
    labelPrefix: String,
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
                id = RowId(index + 1),
                cells =
                    mapOf(
                        LABEL_COLUMN_ID to (labels.getOrNull(index) ?: "$labelPrefix ${index + 1}"),
                        "value" to formatEditorFloat(model.values.getOrElse(index) { 0f }),
                    ),
            )
        }
    return DataTableState(columns = columns, rows = rows, minRows = minRows)
}

/** A table with a label column and one numeric column per series, one row per category. */
internal fun createMultiSeriesTable(
    model: ChartData.MultiSeries,
    minRows: Int,
    labelHeader: String,
): DataTableState {
    val columns =
        listOf(DataTableColumn(id = LABEL_COLUMN_ID, label = labelHeader, numeric = false, weight = 1.4f)) +
            model.series.mapIndexed { index, series ->
                DataTableColumn(
                    id = "series_$index",
                    label = series.name,
                    numeric = true,
                    weight = 1f,
                    defaultValue = "0",
                )
            }
    val rows =
        model.categories.mapIndexed { rowIndex, category ->
            val values =
                model.series.mapIndexed { index, series ->
                    "series_$index" to
                        formatEditorFloat(series.values[rowIndex])
                }
            DataTableRow(id = RowId(rowIndex + 1), cells = mapOf(LABEL_COLUMN_ID to category) + values)
        }
    return DataTableState(columns = columns, rows = rows, minRows = minRows)
}

internal fun validateSingleSeries(
    dataTable: DataTableState,
    chartType: ChartType,
    minRows: Int,
    labelPrefix: String,
    nonNegative: Boolean,
): ValidationResult =
    validateTable(dataTable, chartType, minRows, labelPrefix, nonNegative) { parsed ->
        val valueColumn = parsed.numericColumns.first()
        ChartData.SingleSeries(values = parsed.valuesByColumn.getValue(valueColumn.id), labels = parsed.labels)
    }

/** Each numeric column is a series named by its header; each row is a category. */
internal fun validateMultiSeries(
    dataTable: DataTableState,
    chartType: ChartType,
    minRows: Int,
    labelPrefix: String,
    nonNegative: Boolean,
): ValidationResult =
    validateTable(dataTable, chartType, minRows, labelPrefix, nonNegative) { parsed ->
        ChartData.MultiSeries(
            categories = parsed.labels,
            series =
                parsed.numericColumns.map { column ->
                    ChartData.MultiSeries.Series(
                        name = column.label,
                        values = parsed.valuesByColumn.getValue(column.id),
                    )
                },
        )
    }

/**
 * Checks the table, then builds the chart data from the parsed table. With [nonNegative], negative
 * values are errors, so parsed values are never clamped silently.
 */
private fun validateTable(
    dataTable: DataTableState,
    chartType: ChartType,
    minRows: Int,
    labelPrefix: String,
    nonNegative: Boolean,
    toData: (ParsedEditorTable) -> ChartData,
): ValidationResult {
    val issues =
        editorTableIssues(
            dataTable = dataTable,
            chartName = chartType.validationName,
            minRows = minRows,
            requireNonNegative = nonNegative,
        )
    if (issues.hasErrors()) return invalidValidationResult(issues)
    val parsed =
        parseEditorTable(dataTable = dataTable, labelPrefix = labelPrefix)
            ?: return invalidValidationResult(issues + unsupportedTableIssue())
    return ValidationResult(
        sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
        data = toData(parsed),
        issues = issues,
        appliedRowCount = parsed.labels.size,
    )
}

/** The chart's name in validation messages, e.g. `Multi line chart`. */
private val ChartType.validationName: String
    get() = displayName.lowercase().replaceFirstChar(Char::uppercase) + " chart"

internal fun editorTableIssues(
    dataTable: DataTableState,
    chartName: String,
    minRows: Int,
    requireNonNegative: Boolean,
): List<ValidationIssue> {
    val issues =
        buildList {
            if (dataTable.rows.size < minRows) {
                add(
                    ValidationIssue(
                        code = ValidationIssueCode.TOO_FEW_ROWS,
                        arguments =
                            listOf(
                                ValidationArgument("chart", chartName),
                                ValidationArgument("minimum", minRows.toString()),
                            ),
                    ),
                )
            }

            val labelColumn = dataTable.columns.firstOrNull { column -> !column.numeric }
            if (labelColumn == null) {
                add(ValidationIssue(code = ValidationIssueCode.MISSING_LABEL_COLUMN))
            }

            val numericColumns = dataTable.columns.filter { column -> column.numeric }
            if (numericColumns.isEmpty()) {
                add(ValidationIssue(code = ValidationIssueCode.MISSING_NUMERIC_COLUMN))
            }

            if (labelColumn != null && numericColumns.isNotEmpty()) {
                dataTable.rows.forEach { row ->
                    if (row.cells[labelColumn.id]
                            .orEmpty()
                            .trim()
                            .isBlank()
                    ) {
                        add(
                            ValidationIssue(
                                code = ValidationIssueCode.BLANK_LABEL,
                                severity = ValidationSeverity.WARNING,
                                path = ValidationPath(rowId = row.id, columnId = labelColumn.id),
                            ),
                        )
                    }

                    numericColumns.forEach { column ->
                        val rawValue = row.cells[column.id].orEmpty().trim()
                        when {
                            rawValue.isBlank() ->
                                add(
                                    ValidationIssue(
                                        code = ValidationIssueCode.MISSING_VALUE,
                                        path = ValidationPath(rowId = row.id, columnId = column.id),
                                    ),
                                )
                            rawValue.toFloatOrNull() == null ->
                                add(
                                    ValidationIssue(
                                        code = ValidationIssueCode.INVALID_NUMBER,
                                        path = ValidationPath(rowId = row.id, columnId = column.id),
                                    ),
                                )
                            requireNonNegative && rawValue.toFloat() < 0f ->
                                add(
                                    ValidationIssue(
                                        code = ValidationIssueCode.NEGATIVE_VALUE,
                                        path = ValidationPath(rowId = row.id, columnId = column.id),
                                    ),
                                )
                        }
                    }
                }
            }
        }
    return issues.sortedDeterministically()
}

internal fun List<ValidationIssue>.hasErrors(): Boolean = any { issue -> issue.severity == ValidationSeverity.ERROR }

internal fun invalidValidationResult(issues: List<ValidationIssue>): ValidationResult =
    ValidationResult(
        sanitizedTable = null,
        data = null,
        issues = issues,
    )

private fun unsupportedTableIssue(): ValidationIssue =
    ValidationIssue(code = ValidationIssueCode.UNSUPPORTED_COMBINATION)

internal data class ParsedEditorTable(
    val labels: List<String>,
    val numericColumns: List<DataTableColumn>,
    val valuesByColumn: Map<String, List<Float>>,
    val sanitizedRows: List<DataTableRow>,
)

internal fun parseEditorTable(
    dataTable: DataTableState,
    labelPrefix: String,
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
            valuesByColumn.getValue(column.id) += parsedValue
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
                cells[column.id] = formatEditorFloat(valueProvider().roundToInt().toFloat())
            }
            row.copy(cells = cells)
        }
    return dataTable.copy(rows = rows)
}
