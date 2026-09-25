package presentation.colors

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ColorMathTest {
    @Test
    fun parses_short_long_and_argb_hex() {
        assertEquals("#FF0000", parseHexColor("#f00")?.toHexString())
        assertEquals("#12AB9C", parseHexColor("12ab9c")?.toHexString())
        assertEquals("#12AB9C", parseHexColor("  #FF12AB9C ")?.toHexString())
        assertEquals("#8012AB9C", parseHexColor("#8012ab9c")?.toHexString())
    }

    @Test
    fun hsv_keeps_alpha() {
        val translucent = Color(0x26101010)
        assertEquals(translucent.alpha, translucent.toHsv().alpha)
        assertTrue(translucent.toHsv().toColor().isSameColorAs(translucent))
    }

    @Test
    fun rejects_invalid_hex() {
        assertNull(parseHexColor(""))
        assertNull(parseHexColor("#12345"))
        assertNull(parseHexColor("#GG0000"))
    }

    @Test
    fun hsv_round_trips_to_the_same_color() {
        listOf(Color(0xFF4D90FE), Color(0xFFF43F5E), Color(0xFF000000), Color(0xFFFFFFFF), Color(0xFF808080))
            .forEach { color ->
                assertTrue(color.toHsv().toColor().isSameColorAs(color), "Round trip failed for ${color.toHexString()}")
            }
    }

    @Test
    fun hsv_of_primaries() {
        assertEquals(Hsv(0f, 1f, 1f), Color.Red.toHsv())
        assertEquals(Hsv(120f, 1f, 1f), Color.Green.toHsv())
        assertEquals(Hsv(240f, 1f, 1f), Color.Blue.toHsv())
    }
}
