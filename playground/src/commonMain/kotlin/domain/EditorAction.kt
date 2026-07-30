package domain

sealed interface SettingChange {
    val id: String

    data class BooleanValue(
        override val id: String,
        val value: Boolean,
    ) : SettingChange

    data class FloatValue(
        override val id: String,
        val value: Float,
    ) : SettingChange

    data class TextValue(
        override val id: String,
        val value: String,
    ) : SettingChange

    data class ColorValue(
        override val id: String,
        val value: domain.ColorValue?,
    ) : SettingChange

    data class ColorListValue(
        override val id: String,
        val value: List<domain.ColorValue>?,
    ) : SettingChange
}

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

    data class UpdateCodegenMode(
        val mode: CodegenMode,
    ) : EditorAction

    data class UpdateSetting(
        val change: SettingChange,
    ) : EditorAction

    data class UpdateDataTableCell(
        val rowIndex: Int,
        val columnId: String,
        val value: String,
    ) : EditorAction

    data object AddRow : EditorAction

    data class DeleteRow(
        val rowIndex: Int,
    ) : EditorAction

    data object Randomize : EditorAction

    data object Reset : EditorAction
}
