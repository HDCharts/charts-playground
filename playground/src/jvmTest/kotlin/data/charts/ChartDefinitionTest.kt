package data.charts

import codegen.CODEGEN_GENERATOR_VERSION
import data.ChartCodegenService
import data.chartCatalog
import domain.SettingDescriptor
import domain.ValidationIssueCode
import domain.ValidationPath
import domain.ValidationSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChartDefinitionTest {
    private val codegenService = ChartCodegenService()

    @Test
    fun every_definition_has_valid_defaults_and_generated_code() {
        chartCatalog.charts.forEach { definition ->
            val session = definition.resetSession()
            val validation = definition.validate(session.draft.dataTable)
            val settings = definition.settingsSchema(session)
            val settingIds = settings.mapNotNull { it.settingId() }

            assertEquals(definition.type, session.chartType)
            assertNotNull(validation.data, "${definition.type} default data should be valid")
            assertNotNull(validation.sanitizedTable, "${definition.type} default table should be valid")
            assertTrue(
                codegenService.generate(session.validatedSpec).isNotBlank(),
                "${definition.type} should generate code",
            )
            val artifact = codegenService.generateArtifact(session.validatedSpec)
            assertEquals(codegenService.generate(session.validatedSpec), artifact.source)
            assertEquals("kotlin-compose", artifact.target)
            assertEquals(CODEGEN_GENERATOR_VERSION, artifact.generatorVersion)
            assertTrue(artifact.warnings.isEmpty())
            assertTrue(artifact.source.endsWith("\n"))
            assertEquals(settingIds.size, settingIds.toSet().size, "${definition.type} setting IDs must be unique")
        }
    }

    @Test
    fun every_definition_creates_cells_for_all_columns() {
        chartCatalog.charts.forEach { definition ->
            val session = definition.resetSession()
            val cells = definition.newRowCells(session.draft.dataTable.rows.size, session.draft.dataTable.columns)

            assertEquals(
                session.draft.dataTable.columns
                    .map { it.id }
                    .toSet(),
                cells.keys,
            )
        }
    }

    @Test
    fun every_definition_randomizes_to_valid_data() {
        chartCatalog.charts.forEach { definition ->
            val session = definition.resetSession()
            val validation = definition.validate(definition.randomize(session.draft.dataTable))

            assertNotNull(validation.data, "${definition.type} randomized data should be valid")
            assertNotNull(validation.sanitizedTable, "${definition.type} randomized table should be valid")
        }
    }

    @Test
    fun every_definition_reports_all_invalid_numeric_cells_in_stable_order() {
        chartCatalog.charts.forEach { definition ->
            val table = definition.resetSession().draft.dataTable
            val numericColumn = table.columns.first { column -> column.numeric }
            val firstRow = table.rows.first()
            val secondRow = table.rows[1]
            val invalidTable =
                table.copy(
                    rows =
                        table.rows.map { row ->
                            when (row.id) {
                                firstRow.id -> row.copy(cells = row.cells + (numericColumn.id to ""))
                                secondRow.id -> row.copy(cells = row.cells + (numericColumn.id to "oops"))
                                else -> row
                            }
                        },
                )

            val validation = definition.validate(invalidTable)
            val repeatedValidation = definition.validate(invalidTable)

            assertEquals(validation.issues, repeatedValidation.issues, "${definition.type} issue order must be stable")
            assertEquals(
                listOf(ValidationIssueCode.MISSING_VALUE, ValidationIssueCode.INVALID_NUMBER),
                validation.issues.map { issue -> issue.code },
                "${definition.type} should report every invalid numeric cell",
            )
            assertEquals(setOf(firstRow.id, secondRow.id), validation.invalidRowIds)
            assertEquals(null, validation.data)
            assertEquals(null, validation.sanitizedTable)
        }
    }

    @Test
    fun blank_labels_are_warnings_and_are_normalized() {
        val definition = chartCatalog.definition(domain.ChartType.LINE)
        val table = definition.resetSession().draft.dataTable
        val labelColumn = table.columns.first { column -> !column.numeric }
        val firstRow = table.rows.first()
        val validation =
            definition.validate(
                table.copy(
                    rows =
                        table.rows.map { row ->
                            if (row.id == firstRow.id) {
                                row.copy(cells = row.cells + (labelColumn.id to "  "))
                            } else {
                                row
                            }
                        },
                ),
            )

        assertEquals(ValidationSeverity.WARNING, validation.issues.single().severity)
        assertEquals(ValidationIssueCode.BLANK_LABEL, validation.issues.single().code)
        assertTrue(validation.isValid)
        assertNotNull(validation.data)
        assertTrue(validation.invalidRowIds.isEmpty())
    }

    @Test
    fun non_negative_chart_types_reject_negative_values_with_row_paths() {
        val nonNegativeTypes =
            setOf(
                domain.ChartType.PIE,
                domain.ChartType.HISTOGRAM,
                domain.ChartType.STACKED_BAR,
                domain.ChartType.AREA,
                domain.ChartType.RADAR,
            )

        chartCatalog.charts.forEach { definition ->
            val table = definition.resetSession().draft.dataTable
            val numericColumn = table.columns.first { column -> column.numeric }
            val firstRow = table.rows.first()
            val invalidTable =
                table.copy(
                    rows =
                        table.rows.map { row ->
                            if (row.id == firstRow.id) {
                                row.copy(cells = row.cells + (numericColumn.id to "-1"))
                            } else {
                                row
                            }
                        },
                )
            val validation = definition.validate(invalidTable)

            if (definition.type in nonNegativeTypes) {
                assertEquals(ValidationIssueCode.NEGATIVE_VALUE, validation.issues.single().code)
                assertEquals(
                    ValidationPath(rowId = firstRow.id, columnId = numericColumn.id),
                    validation.issues.single().path,
                )
                assertEquals(setOf(firstRow.id), validation.invalidRowIds)
            } else {
                assertTrue(validation.isValid, "${definition.type} should allow negative values")
            }
        }
    }

    @Test
    fun structural_validation_reports_minimum_rows_and_missing_columns() {
        val definition = chartCatalog.definition(domain.ChartType.LINE)
        val table = definition.resetSession().draft.dataTable

        val tooFewRows = definition.validate(table.copy(rows = table.rows.take(1)))
        assertEquals(ValidationIssueCode.TOO_FEW_ROWS, tooFewRows.issues.single().code)
        assertEquals(
            "2",
            tooFewRows.issues
                .single()
                .arguments
                .single { it.name == "minimum" }
                .value,
        )

        val noNumericColumns =
            definition.validate(
                table.copy(
                    columns = table.columns.filter { column -> !column.numeric },
                ),
            )
        assertEquals(ValidationIssueCode.MISSING_NUMERIC_COLUMN, noNumericColumns.issues.single().code)

        val noLabelColumns =
            definition.validate(
                table.copy(
                    columns = table.columns.filter { column -> column.numeric },
                ),
            )
        assertEquals(ValidationIssueCode.MISSING_LABEL_COLUMN, noLabelColumns.issues.single().code)
    }
}

private fun SettingDescriptor.settingId(): String? =
    when (this) {
        is SettingDescriptor.Section -> null
        SettingDescriptor.Divider -> null
        is SettingDescriptor.Toggle -> id
        is SettingDescriptor.Slider -> id
        is SettingDescriptor.Dropdown -> id
        is SettingDescriptor.Color -> id
        is SettingDescriptor.ColorPalette -> id
    }
