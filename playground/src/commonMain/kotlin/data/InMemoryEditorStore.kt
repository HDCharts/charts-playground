package data

import domain.ChartCatalog
import domain.ChartDefinition
import domain.ChartEditorState
import domain.ChartSession
import domain.CodegenMode
import domain.DataTableState
import domain.EditorAction
import domain.EditorStore
import domain.RightPanelTab
import domain.SettingChange
import domain.SettingDescriptor
import domain.SnapshotPublishMetadata
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
                    session.copy(title = action.title)
                }
            is EditorAction.UpdateCodegenMode ->
                updateCurrentSession(state) { session, _ ->
                    session.copy(codegenMode = action.mode)
                }
            is EditorAction.UpdateSetting ->
                updateCurrentSession(state) { session, _ ->
                    updateSetting(session, action.change)
                }
            is EditorAction.UpdateDataTableCell ->
                updateCurrentSession(state) { session, definition ->
                    val updatedTable =
                        session.dataTable.updateCell(
                            rowIndex = action.rowIndex,
                            columnId = action.columnId,
                            value = action.value,
                        )
                    applyValidation(session, definition, updatedTable)
                }
            EditorAction.AddRow ->
                updateCurrentSession(state) { session, definition ->
                    val rowIndex = session.dataTable.rows.size
                    val cells = definition.newRowCells(rowIndex, session.dataTable.columns)
                    applyValidation(session, definition, session.dataTable.withAddedRow(cells))
                }
            is EditorAction.DeleteRow ->
                updateCurrentSession(state) { session, definition ->
                    applyValidation(session, definition, session.dataTable.withDeletedRow(action.rowIndex))
                }
            EditorAction.Randomize ->
                updateCurrentSession(state) { session, definition ->
                    applyValidation(session, definition, definition.randomize(session.dataTable))
                }
            EditorAction.Reset ->
                updateCurrentSession(state) { session, definition ->
                    newSession(definition, session.codegenMode)
                }
        }

    private fun updateCurrentSession(
        state: ChartEditorState,
        update: (ChartSession, ChartDefinition) -> ChartSession,
    ): ChartEditorState {
        val chartType = state.selectedChartType
        val definition = catalog.definition(chartType)
        val current = state.sessions.getValue(chartType)
        val next = refresh(definition, update(current, definition))
        return state.copy(sessions = state.sessions + (chartType to next))
    }

    private fun updateSetting(
        session: ChartSession,
        change: SettingChange,
    ): ChartSession {
        val descriptor = session.settings.firstOrNull { descriptor -> descriptor.matches(change) } ?: return session
        val nextStyle = descriptor.applyChange(session.styleState, change) ?: return session
        return session.copy(styleState = nextStyle)
    }

    private fun applyValidation(
        session: ChartSession,
        definition: ChartDefinition,
        updatedTable: DataTableState,
    ): ChartSession {
        val result = definition.validate(updatedTable)
        val nextData = result.data
        val nextTable = result.sanitizedTable
        if (nextData == null || nextTable == null) {
            return session.copy(
                dataTable = updatedTable,
                validationMessage = result.message,
                invalidRowIds = result.invalidRowIds,
            )
        }

        return session.copy(
            dataTable = nextTable,
            data = nextData,
            validationMessage = result.message,
            invalidRowIds = emptySet(),
        )
    }

    private fun newSession(
        definition: ChartDefinition,
        codegenMode: CodegenMode,
    ): ChartSession = refresh(definition, definition.resetSession(codegenMode))

    private fun refresh(
        definition: ChartDefinition,
        session: ChartSession,
    ): ChartSession =
        session.copy(
            settings = definition.settingsSchema(session),
            generatedCode = codegenService.generate(session),
        )
}

private fun defaultEditorState(
    catalog: ChartCatalog,
    snapshotMetadata: SnapshotPublishMetadata?,
    codegenService: ChartCodegenService,
): ChartEditorState {
    val sessions =
        catalog.charts.associate { definition ->
            definition.type to initialSession(definition, codegenService)
        }
    val initialType = catalog.primaryChartTypes.firstOrNull() ?: catalog.charts.first().type
    return ChartEditorState(
        selectedChartType = initialType,
        rightPanelTab = RightPanelTab.SETTINGS,
        sessions = sessions,
        primaryChartTypes = catalog.primaryChartTypes,
        overflowChartTypes = catalog.overflowChartTypes,
        snapshotMetadata = snapshotMetadata,
    )
}

private fun SettingDescriptor.matches(change: SettingChange): Boolean =
    when (this) {
        is SettingDescriptor.Section,
        SettingDescriptor.Divider,
        -> false
        is SettingDescriptor.Toggle -> id == change.id
        is SettingDescriptor.Slider -> id == change.id
        is SettingDescriptor.Dropdown -> id == change.id
        is SettingDescriptor.Color -> id == change.id
        is SettingDescriptor.ColorPalette -> id == change.id
    }

private fun SettingDescriptor.applyChange(
    style: domain.ChartStyleState,
    change: SettingChange,
): domain.ChartStyleState? =
    when {
        this is SettingDescriptor.Toggle && change is SettingChange.BooleanValue -> write(style, change.value)
        this is SettingDescriptor.Slider && change is SettingChange.FloatValue -> write(style, change.value)
        this is SettingDescriptor.Dropdown && change is SettingChange.TextValue -> write(style, change.value)
        this is SettingDescriptor.Color && change is SettingChange.ColorValue -> write(style, change.value)
        this is SettingDescriptor.ColorPalette && change is SettingChange.ColorListValue -> write(style, change.value)
        else -> null
    }

private fun initialSession(
    definition: ChartDefinition,
    codegenService: ChartCodegenService,
): ChartSession =
    definition.resetSession(codegenMode = CodegenMode.MINIMAL).let { session ->
        session.copy(
            settings = definition.settingsSchema(session),
            generatedCode = codegenService.generate(session),
        )
    }
