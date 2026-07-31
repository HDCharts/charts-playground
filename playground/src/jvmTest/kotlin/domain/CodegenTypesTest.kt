package domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodegenTypesTest {
    @Test
    fun derived_function_names_are_valid_when_titles_contain_keywords_or_digits() {
        val keywordTitle = deriveFunctionName("class", ChartType.LINE)
        val digitTitle = deriveFunctionName("123", ChartType.BAR)

        assertTrue(keywordTitle.matches(Regex("[A-Za-z_][A-Za-z0-9_]*")))
        assertTrue(digitTitle.matches(Regex("[A-Za-z_][A-Za-z0-9_]*")))
        assertFalse(keywordTitle in KOTLIN_KEYWORDS)
        assertFalse(digitTitle in KOTLIN_KEYWORDS)
    }
}

private val KOTLIN_KEYWORDS =
    setOf(
        "as",
        "break",
        "class",
        "continue",
        "do",
        "else",
        "false",
        "for",
        "fun",
        "if",
        "in",
        "interface",
        "is",
        "null",
        "object",
        "package",
        "return",
        "super",
        "this",
        "throw",
        "true",
        "try",
        "typealias",
        "typeof",
        "val",
        "var",
        "when",
        "while",
    )
