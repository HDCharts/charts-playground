package presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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

// A chart switch builds one panel per frame, chart last, so no frame is long and the chart's start
// animation begins on a quiet frame.
private const val TABLE_STAGE = 1
private const val SETTINGS_STAGE = 2
private const val CHART_STAGE = 3

@Composable
internal fun EditorWorkspace(
    state: ChartEditorState,
    session: ChartSession,
    chartType: ChartType,
    onAction: (EditorAction) -> Unit,
    onCopyCode: suspend (String) -> Boolean,
    wideLayout: Boolean,
) {
    val stage = rememberBuildStage(chartType)

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
        if (stage < CHART_STAGE) return PanelPlaceholder(modifier)
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
        if (stage < SETTINGS_STAGE) return PanelPlaceholder(modifier)
        RightPanel(
            tab = state.rightPanelTab,
            onTabChange = { tab -> onAction(EditorAction.SelectRightPanelTab(tab)) },
            settingsContent = {
                SettingsPanel(
                    session = session,
                    descriptors = session.settings,
                    onSettingChange = { change -> onAction(EditorAction.UpdateSetting(change)) },
                    scrollable = wideLayout,
                    modifier = if (wideLayout) Modifier.fillMaxWidth().fillMaxHeight() else Modifier.fillMaxWidth(),
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

@Composable
private fun rememberBuildStage(chartType: ChartType): Int {
    var stage by remember(chartType) { mutableIntStateOf(TABLE_STAGE) }
    LaunchedEffect(chartType) {
        while (stage < CHART_STAGE) {
            withFrameNanos { }
            stage++
        }
    }
    return stage
}

@Composable
private fun PanelPlaceholder(modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp) {}
}
