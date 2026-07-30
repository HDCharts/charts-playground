package domain

sealed interface SettingDescriptor {
    data class Section(
        val title: String,
    ) : SettingDescriptor

    data object Divider : SettingDescriptor

    data class Toggle(
        val id: String,
        val label: String,
        val defaultValue: Boolean,
        val read: (ChartStyleState) -> Boolean?,
        val write: (ChartStyleState, Boolean?) -> ChartStyleState,
    ) : SettingDescriptor

    data class Slider(
        val id: String,
        val label: String,
        val defaultValue: Float,
        val min: Float,
        val max: Float,
        val steps: Int,
        val read: (ChartStyleState) -> Float?,
        val write: (ChartStyleState, Float?) -> ChartStyleState,
        val format: (Float) -> String = { value -> value.toString() },
    ) : SettingDescriptor

    data class Dropdown(
        val id: String,
        val label: String,
        val options: List<DropdownOption>,
        val defaultValue: String,
        val read: (ChartStyleState) -> String?,
        val write: (ChartStyleState, String?) -> ChartStyleState,
    ) : SettingDescriptor

    data class Color(
        val id: String,
        val label: String,
        val read: (ChartStyleState) -> ColorValue?,
        val write: (ChartStyleState, ColorValue?) -> ChartStyleState,
    ) : SettingDescriptor

    data class ColorPalette(
        val id: String,
        val title: String,
        val itemCount: (ChartSession) -> Int,
        val read: (ChartStyleState) -> List<ColorValue>?,
        val write: (ChartStyleState, List<ColorValue>?) -> ChartStyleState,
    ) : SettingDescriptor
}

data class DropdownOption(
    val label: String,
    val value: String,
)
