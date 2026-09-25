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
    val snapshotMetadata: SnapshotMetadataUi? = null,
)
