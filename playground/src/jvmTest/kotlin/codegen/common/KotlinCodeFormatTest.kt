package codegen.common

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KotlinCodeFormatTest {
    @Test
    fun float_literals_are_rounded_and_locale_independent() {
        assertEquals("1.2346f", formatKotlinFloatLiteral(1.234567f))
        assertEquals("-1.2346f", formatKotlinFloatLiteral(-1.234567f))

        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("1.2346f", formatKotlinFloatLiteral(1.234567f))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun float_literals_reject_non_finite_values_and_support_extreme_values() {
        assertFailsWith<IllegalArgumentException> { formatKotlinFloatLiteral(Float.NaN) }
        assertFailsWith<IllegalArgumentException> { formatKotlinFloatLiteral(Float.POSITIVE_INFINITY) }

        val extremeLiteral = formatKotlinFloatLiteral(Float.MAX_VALUE)
        assertTrue(extremeLiteral.endsWith("f"))
        assertTrue("NaN" !in extremeLiteral)
        assertTrue("Infinity" !in extremeLiteral)
    }

    @Test
    fun style_arguments_have_stable_name_order() {
        val arguments =
            resolveStyleArguments(
                styleProperties =
                    codegen.StylePropertiesSnapshot(
                        current = listOf(codegen.styleProperty("zIndex", 1f), codegen.styleProperty("alpha", 0.5f)),
                        defaults = emptyList(),
                    ),
            )

        assertEquals(listOf("alpha = 0.5f,", "zIndex = 1f,"), arguments.map { it.code })
    }
}
