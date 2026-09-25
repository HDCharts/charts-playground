package data.charts

import domain.SettingDescriptor
import domain.StyleResolver
import domain.StyleSetting
import domain.StyleValue
import domain.visibleFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingVisibilityTest {
    private val settings = LineChartDefinition.settings

    @Test
    fun point_settings_hidden_while_points_are_off() {
        val visible = visiblePaths("points.visible" to false)

        assertTrue("Points" in visible, "The Points header stays so points can be switched on")
        assertFalse("points.size" in visible)
        assertFalse("points.color" in visible)
    }

    @Test
    fun point_settings_shown_while_points_are_on() {
        val visible = visiblePaths("points.visible" to true)

        assertTrue("points.size" in visible)
        assertTrue("points.color" in visible)
    }

    @Test
    fun selection_marker_settings_follow_points_or_drag_point() {
        val neither = visiblePaths("points.visible" to false, "selection.visible" to false)
        val pointsOnly = visiblePaths("points.visible" to true, "selection.visible" to false)

        assertFalse("selection.size" in neither)
        assertFalse("selection.activeSize" in neither)
        assertFalse("selection.color" in neither)
        assertFalse("selection.size" in pointsOnly)
        assertTrue("selection.activeSize" in pointsOnly)
        assertTrue("selection.color" in pointsOnly)
    }

    @Test
    fun range_bounds_hidden_until_fixed_range_is_on() {
        assertFalse("range.min" in visiblePaths("range.fixed" to false))
        assertTrue("range.min" in visiblePaths("range.fixed" to true))
    }

    @Test
    fun section_toggle_is_not_repeated_as_a_row() {
        val visible = settings.visibleFor(resolver("points.visible" to true))

        assertEquals(0, visible.count { it is StyleSetting && it.path == "points.visible" })
    }

    private fun visiblePaths(vararg switches: Pair<String, Boolean>): Set<String> =
        settings
            .visibleFor(resolver(*switches))
            .map { descriptor ->
                when (descriptor) {
                    is SettingDescriptor.Section -> descriptor.title
                    is StyleSetting -> descriptor.path
                }
            }.toSet()

    private fun resolver(vararg switches: Pair<String, Boolean>): StyleResolver {
        val values = switches.toMap()
        return StyleResolver { path -> values[path]?.let(StyleValue::Bool) }
    }
}
