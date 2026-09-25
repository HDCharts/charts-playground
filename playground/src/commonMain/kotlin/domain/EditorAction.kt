package domain

/** Sets a style value, or resets it to the default when [value] is null. */
data class SettingChange(
    val path: String,
    val value: StyleValue?,
)

sealed interface EditorAction {
    data class SelectChart(
        val chartType: ChartType,
    ) : EditorAction

    data class SelectRightPanelTab(
        val tab: RightPanelTab,
    ) : EditorAction

    data class UpdateTitle(
        val title: String,
    ) : EditorAction

    data class UpdateSetting(
        val change: SettingChange,
    ) : EditorAction

    data class UpdateDataTableCell(
        val rowId: RowId,
        val columnId: String,
        val value: String,
    ) : EditorAction

    data object AddRow : EditorAction

    data class DeleteRow(
        val rowId: RowId,
    ) : EditorAction

    data object Randomize : EditorAction

    data object Reset : EditorAction
}
