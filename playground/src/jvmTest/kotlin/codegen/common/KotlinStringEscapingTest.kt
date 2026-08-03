package codegen.common

import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinStringEscapingTest {
    @Test
    fun kotlin_string_escaping_escapes_dollar_sign() {
        assertEquals("\\$", escapeKotlinString("$"))
        assertEquals("\\$" + "{value}", escapeKotlinString("$" + "{value}"))
    }

    @Test
    fun kotlin_string_escaping_handles_quotes_slashes_control_characters_and_unicode() {
        assertEquals(
            "quote: \\\" slash: \\\\ newline: \\n tab: \\t control: \\u0001 snowman: ☃",
            escapeKotlinString("quote: \" slash: \\ newline: \n tab: \t control: \u0001 snowman: ☃"),
        )
    }
}
