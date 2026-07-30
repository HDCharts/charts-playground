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
import domain.EditorAction
import presentation.chart.ChartPanel
import presentation.code.CodePreviewPanel

@Composable
internal fun EditorWorkspace(
    state: ChartEditorState,
    session: ChartSession,
    chartType: ChartType,
    onAction: (EditorAction) -> Unit,
    wideLayout: Boolean,
) {
    val settings = session.settings

    @Composable
    fun dataTable(modifier: Modifier) {
        DataTableEditor(
            dataTable = session.dataTable,
            validationMessage = session.validationMessage,
            invalidRowIds = session.invalidRowIds,
            onCellChange = { rowIndex, columnId, value ->
                onAction(
                    EditorAction.UpdateDataTableCell(
                        rowIndex = rowIndex,
                        columnId = columnId,
                        value = value,
                    ),
                )
            },
            onAddRow = { onAction(EditorAction.AddRow) },
            onDeleteRow = { rowIndex -> onAction(EditorAction.DeleteRow(rowIndex)) },
            onRandomize = { onAction(EditorAction.Randomize) },
            onReset = { onAction(EditorAction.Reset) },
            expandToFillHeight = wideLayout,
            modifier = modifier,
        )
    }

    @Composable
    fun chart(modifier: Modifier) {
        ChartPanel(
            session = session,
            chartType = chartType,
            onTitleChange = { title -> onAction(EditorAction.UpdateTitle(title)) },
            expandToFillHeight = wideLayout,
            modifier = modifier,
        )
    }

    @Composable
    fun rightPanel(modifier: Modifier) {
        RightPanel(
            tab = state.rightPanelTab,
            onTabChange = { tab -> onAction(EditorAction.SelectRightPanelTab(tab)) },
            settingsContent = {
                SettingsPanel(
                    session = session,
                    descriptors = settings,
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
                    mode = session.codegenMode,
                    onModeChange = { mode -> onAction(EditorAction.UpdateCodegenMode(mode)) },
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
            dataTable(Modifier.weight(30f).fillMaxHeight())
            chart(Modifier.weight(40f).fillMaxHeight().padding(start = 16.dp))
            rightPanel(Modifier.weight(30f).fillMaxHeight().padding(start = 16.dp))
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            dataTable(Modifier.fillMaxWidth())
            chart(Modifier.fillMaxWidth().padding(top = 16.dp))
            rightPanel(Modifier.fillMaxWidth().padding(top = 16.dp))
        }
    }
}
