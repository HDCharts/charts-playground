package data

import domain.ChartCatalog
import domain.ChartDefinition
import domain.ChartEditorState
import domain.ChartSession
import domain.ChartValidationState
import domain.DataTableState
import domain.EditorAction
import domain.EditorStore
import domain.RightPanelTab
import domain.SettingChange
import domain.SnapshotPublishMetadata
import domain.ValidatedChartSpec
import domain.styleSettings
import domain.toUI
import domain.updateCell
import domain.withAddedRow
import domain.withDeletedRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryEditorStore(
    private val catalog: ChartCatalog = chartCatalog,
    snapshotMetadata: SnapshotPublishMetadata? = null,
    private val codegenService: ChartCodegenService = ChartCodegenService(),
) : EditorStore {
    private val _state = MutableStateFlow(defaultEditorState(catalog, snapshotMetadata, codegenService))

    override val state = _state.asStateFlow()

    override fun dispatch(action: EditorAction) {
        _state.update { current -> reduce(current, action) }
    }

    private fun reduce(
        state: ChartEditorState,
        action: EditorAction,
    ): ChartEditorState =
        when (action) {
            is EditorAction.SelectChart -> state.copy(selectedChartType = action.chartType)
            is EditorAction.SelectRightPanelTab -> state.copy(rightPanelTab = action.tab)
            is EditorAction.UpdateTitle ->
                updateCurrentSession(state) { session, _ ->
                    val draft = session.draft.copy(title = action.title)
                    session.copy(
                        draft = draft,
                        validatedSpec = session.validatedSpec.copy(title = action.title),
                    )
                }
            is EditorAction.UpdateSetting ->
                updateCurrentSession(state) { session, _ ->
                    updateSetting(session, action.change)
                }
            is EditorAction.UpdateDataTableCell ->
                updateCurrentSession(state) { session, definition ->
                    val updatedTable =
                        session.draft.dataTable.updateCell(
                            rowId = action.rowId,
                            columnId = action.columnId,
                            value = action.value,
                        )
                    applyValidation(session, definition, updatedTable)
                }
            EditorAction.AddRow ->
                updateCurrentSession(state) { session, definition ->
                    val rowIndex = session.draft.dataTable.rows.size
                    val cells = definition.newRowCells(rowIndex, session.draft.dataTable.columns)
                    applyValidation(session, definition, session.draft.dataTable.withAddedRow(cells))
                }
            is EditorAction.DeleteRow ->
                updateCurrentSession(state) { session, definition ->
                    applyValidation(session, definition, session.draft.dataTable.withDeletedRow(action.rowId))
                }
            EditorAction.Randomize ->
                updateCurrentSession(state) { session, definition ->
                    applyValidation(session, definition, definition.randomize(session.draft.dataTable))
                }
            EditorAction.Reset ->
                updateCurrentSession(state) { _, definition ->
                    definition.resetSession()
                }
        }

    private fun updateCurrentSession(
        state: ChartEditorState,
        update: (ChartSession, ChartDefinition) -> ChartSession,
    ): ChartEditorState {
        val chartType = state.selectedChartType
        val definition = catalog.definition(chartType)
        val current = state.sessions.getValue(chartType)
        val next = update(current, definition).withGeneratedCode(codegenService)
        return state.copy(sessions = state.sessions + (chartType to next))
    }

    private fun updateSetting(
        session: ChartSession,
        change: SettingChange,
    ): ChartSession {
        if (session.settings.styleSettings.none { it.path == change.path }) return session
        val nextStyle = session.draft.styleState.with(change.path, change.value)
        return session.copy(
            draft = session.draft.copy(styleState = nextStyle),
            validatedSpec = session.validatedSpec.copy(styleState = nextStyle),
        )
    }

    private fun applyValidation(
        session: ChartSession,
        definition: ChartDefinition,
        updatedTable: DataTableState,
    ): ChartSession {
        val result = definition.validate(updatedTable)
        val nextDraft = session.draft.copy(dataTable = updatedTable)
        val nextData = result.data
        val nextTable = result.sanitizedTable
        if (nextData == null || nextTable == null || !result.isValid) {
            return session.copy(
                draft = nextDraft,
                validatedSpec =
                    session.validatedSpec.copy(
                        title = nextDraft.title,
                        styleState = nextDraft.styleState,
                    ),
                validation = ChartValidationState.Invalid(result.issues),
            )
        }

        return session.copy(
            draft = nextDraft,
            validatedSpec =
                ValidatedChartSpec(
                    chartType = session.chartType,
                    title = nextDraft.title,
                    data = nextData,
                    styleState = nextDraft.styleState,
                ),
            validation = ChartValidationState.Valid(result.issues, result.appliedRowCount),
        )
    }
}

private fun defaultEditorState(
    catalog: ChartCatalog,
    snapshotMetadata: SnapshotPublishMetadata?,
    codegenService: ChartCodegenService,
): ChartEditorState {
    val sessions =
        catalog.charts.associate { definition ->
            definition.type to definition.resetSession().withGeneratedCode(codegenService)
        }
    val initialType = catalog.chartTypes.firstOrNull() ?: catalog.charts.first().type
    return ChartEditorState(
        selectedChartType = initialType,
        rightPanelTab = RightPanelTab.SETTINGS,
        sessions = sessions,
        chartTypes = catalog.chartTypes,
        snapshotMetadata = snapshotMetadata?.toUI(),
    )
}

private fun ChartSession.withGeneratedCode(codegenService: ChartCodegenService): ChartSession =
    copy(generatedCode = codegenService.generate(validatedSpec))
