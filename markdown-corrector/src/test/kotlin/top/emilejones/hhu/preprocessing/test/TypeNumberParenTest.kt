package top.emilejones.hhu.preprocessing.test

import kotlin.test.Test
import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeNumberParenTest {

    private fun shouldMatchProvider() = listOf(
        "1） Title",
        "10） 标题",
        "# 1） 标题",
        "100） 三位数"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1. Title",
        "(1) Title",
        "1) Title", // Regex uses Chinese parenthesis ）: ^(?:#+\\s*)?([0-9]{1,3}）\\s*(.+)$
        "Title 1）"
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_NUMBER_PAREN.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_NUMBER_PAREN.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }
}
