package domain

enum class RightPanelTab {
    SETTINGS,
    CODE,
}

data class ChartEditorState(
    val selectedChartType: ChartType,
    val rightPanelTab: RightPanelTab,
    val sessions: Map<ChartType, ChartSession>,
    val primaryChartTypes: List<ChartType>,
    val overflowChartTypes: List<ChartType>,
    val snapshotMetadata: SnapshotPublishMetadata? = null,
)
