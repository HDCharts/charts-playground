package data

import dev.hdcode.charts.sampleshared.data.BarSampleUseCase
import dev.hdcode.charts.sampleshared.data.HistogramSampleUseCase
import dev.hdcode.charts.sampleshared.data.LineSampleUseCase
import dev.hdcode.charts.sampleshared.data.MultiLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.PieSampleUseCase
import dev.hdcode.charts.sampleshared.data.RadarSampleUseCase
import dev.hdcode.charts.sampleshared.data.StackedAreaSampleUseCase
import dev.hdcode.charts.sampleshared.data.StackedBarSampleUseCase
import dev.hdcode.charts.sampleshared.data.barSampleUseCase
import dev.hdcode.charts.sampleshared.data.histogramSampleUseCase
import dev.hdcode.charts.sampleshared.data.lineSampleUseCase
import dev.hdcode.charts.sampleshared.data.multiLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.pieSampleUseCase
import dev.hdcode.charts.sampleshared.data.radarSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedAreaSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedBarSampleUseCase
import domain.ChartData
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
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.PieSlice
import kotlin.math.max
import kotlin.math.roundToInt

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

internal fun List<PieSlice>.toSingleSeries(): ChartData.SingleSeries =
    ChartData.SingleSeries(
        values = map { it.value },
        labels = map { it.label }.takeIf { it.isNotEmpty() },
    )

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
                id = RowId(index + 1),
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
    val issues =
        editorTableIssues(
            dataTable = dataTable,
            chartName = chartName,
            minRows = minRows,
            requireNonNegative = clampToPositive,
        )
    if (issues.hasErrors()) return invalidValidationResult(issues)
    val parsed =
        parseEditorTable(
            dataTable = dataTable,
            labelPrefix = labelPrefix,
            clampToPositive = clampToPositive,
        ) ?: return invalidValidationResult(issues + unsupportedTableIssue())

    val valueColumn = parsed.numericColumns.firstOrNull() ?: return invalidValidationResult(issues)
    val values = parsed.valuesByColumn.getValue(valueColumn.id)

    return ValidationResult(
        sanitizedTable = dataTable.copy(rows = parsed.sanitizedRows),
        data = ChartData.SingleSeries(values = values, labels = parsed.labels),
        issues = issues,
        appliedRowCount = parsed.labels.size,
    )
}

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
