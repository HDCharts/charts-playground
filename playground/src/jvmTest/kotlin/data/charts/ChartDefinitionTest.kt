package data.charts

import data.ChartCodegenService
import data.chartCatalog
import domain.SettingDescriptor
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
            val validation = definition.validate(session.dataTable)
            val settings = definition.settingsSchema(session)
            val settingIds = settings.mapNotNull { it.settingId() }

            assertEquals(definition.type, session.chartType)
            assertNotNull(validation.data, "${definition.type} default data should be valid")
            assertNotNull(validation.sanitizedTable, "${definition.type} default table should be valid")
            assertTrue(
                codegenService.generate(session).isNotBlank(),
                "${definition.type} should generate code",
            )
            assertEquals(settingIds.size, settingIds.toSet().size, "${definition.type} setting IDs must be unique")
        }
    }

    @Test
    fun every_definition_creates_cells_for_all_columns() {
        chartCatalog.charts.forEach { definition ->
            val session = definition.resetSession()
            val cells = definition.newRowCells(session.dataTable.rows.size, session.dataTable.columns)

            assertEquals(
                session.dataTable.columns
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
            val validation = definition.validate(definition.randomize(session.dataTable))

            assertNotNull(validation.data, "${definition.type} randomized data should be valid")
            assertNotNull(validation.sanitizedTable, "${definition.type} randomized table should be valid")
        }
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
