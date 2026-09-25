package presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.ChartEditorState
import domain.ChartSession
import domain.ChartType
import domain.ChartValidationState
import domain.EditorAction
import presentation.chart.ChartPanel
import presentation.code.CodePreviewPanel

// Fixed for every chart type so switching charts never shifts the panels; wide tables scroll instead.
private const val DATA_TABLE_PANEL_WEIGHT = 30f
private const val CHART_PANEL_WEIGHT = 40f
private const val RIGHT_PANEL_WEIGHT = 30f

@Composable
internal fun EditorWorkspace(
    state: ChartEditorState,
    session: ChartSession,
    chartType: ChartType,
    onAction: (EditorAction) -> Unit,
    onCopyCode: suspend (String) -> Boolean,
    wideLayout: Boolean,
) {
    @Composable
    fun dataTableContent(modifier: Modifier) {
        DataTableEditor(
            dataTable = session.draft.dataTable,
            validationMessage = session.validation.presentationMessage(),
            validationIsBlocking = session.validation is ChartValidationState.Invalid,
            invalidRowIds = session.validation.invalidRowIds,
            invalidCellPaths = session.validation.invalidPaths,
            onCellChange = { rowId, columnId, value ->
                onAction(
                    EditorAction.UpdateDataTableCell(
                        rowId = rowId,
                        columnId = columnId,
                        value = value,
                    ),
                )
            },
            onAddRow = { onAction(EditorAction.AddRow) },
            onDeleteRow = { rowId -> onAction(EditorAction.DeleteRow(rowId)) },
            onRandomize = { onAction(EditorAction.Randomize) },
            onReset = { onAction(EditorAction.Reset) },
            expandToFillHeight = wideLayout,
            modifier = modifier,
        )
    }

    @Composable
    fun chartContent(modifier: Modifier) {
        ChartPanel(
            session = session,
            chartType = chartType,
            onTitleChange = { title -> onAction(EditorAction.UpdateTitle(title)) },
            expandToFillHeight = wideLayout,
            modifier = modifier,
        )
    }

    @Composable
    fun rightPanelContent(modifier: Modifier) {
        RightPanel(
            tab = state.rightPanelTab,
            onTabChange = { tab -> onAction(EditorAction.SelectRightPanelTab(tab)) },
            settingsContent = {
                SettingsPanel(
                    session = session,
                    descriptors = session.settings,
                    onSettingChange = { change -> onAction(EditorAction.UpdateSetting(change)) },
                    modifier =
                        if (wideLayout) {
                            Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState())
                        } else {
                            Modifier.fillMaxWidth()
                        },
                )
            },
            codeContent = {
                CodePreviewPanel(
                    code = session.generatedCode,
                    onCopyCode = onCopyCode,
                    expandToFillHeight = wideLayout,
                    showTitle = false,
                    modifier =
                        if (wideLayout) {
                            Modifier.fillMaxWidth().fillMaxHeight()
                        } else {
                            Modifier.fillMaxWidth()
                        },
                )
            },
            showTabSelector = true,
            expandToFillHeight = wideLayout,
            modifier = modifier,
        )
    }

    if (wideLayout) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            dataTableContent(Modifier.weight(DATA_TABLE_PANEL_WEIGHT).fillMaxHeight())
            chartContent(Modifier.weight(CHART_PANEL_WEIGHT).fillMaxHeight().padding(start = 16.dp))
            rightPanelContent(Modifier.weight(RIGHT_PANEL_WEIGHT).fillMaxHeight().padding(start = 16.dp))
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            dataTableContent(Modifier.fillMaxWidth())
            chartContent(Modifier.fillMaxWidth().padding(top = 16.dp))
            rightPanelContent(Modifier.fillMaxWidth().padding(top = 16.dp))
        }
    }
}
