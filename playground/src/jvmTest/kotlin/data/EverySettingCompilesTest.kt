package data

import domain.EditorAction
import testing.assertSnippetCompiles
import testing.changeEverySetting
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * For every chart, changes every setting away from its default and compiles the generated code.
 * Catches settings whose code generation does not match the charts library API.
 */
class EverySettingCompilesTest {
    @Test
    fun generated_code_compiles_with_every_setting_changed() {
        val store = InMemoryEditorStore(chartCatalog, snapshotMetadata = null)

        val failures =
            chartCatalog.charts.mapNotNull { definition ->
                store.dispatch(EditorAction.SelectChart(definition.type))
                val session =
                    store.state.value.sessions
                        .getValue(definition.type)
                session.settings.changeEverySetting(session.validatedSpec.data).forEach { change ->
                    store.dispatch(EditorAction.UpdateSetting(change))
                }

                val code =
                    store.state.value.sessions
                        .getValue(definition.type)
                        .generatedCode
                runCatching { assertSnippetCompiles(code, definition.type.name) }.exceptionOrNull()?.message
            }

        assertTrue(failures.isEmpty(), failures.joinToString(separator = "\n\n"))
    }
}
