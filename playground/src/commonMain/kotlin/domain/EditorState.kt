package domain

enum class RightPanelTab {
    SETTINGS,
    CODE,
}

data class ChartEditorState(
    val selectedChartType: ChartType,
    val rightPanelTab: RightPanelTab,
    val sessions: Map<ChartType, ChartSession>,
    val chartTypes: List<ChartType>,
    val snapshotMetadata: SnapshotPublishMetadata? = null,
)

fun ChartEditorState.withRightPanelTab(
    tab: RightPanelTab,
    generateCode: (ValidatedChartSpec) -> String,
): ChartEditorState {
    val next = copy(rightPanelTab = tab)
    val session = sessions.getValue(selectedChartType)
    if (tab != RightPanelTab.CODE || session.generatedCode.isNotEmpty()) return next
    val withCode = session.copy(generatedCode = generateCode(session.validatedSpec))
    return next.copy(sessions = sessions + (selectedChartType to withCode))
}
