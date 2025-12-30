package top.emilejones.hhu.preprocessing.test

import kotlin.test.Test
import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeParenthesizedNumberTest {

    private fun shouldMatchProvider() = listOf(
        "(1) 标题",
        "（2） 标题",
        "(10) 两位数",
        "# (1) markdown标题",
        "（100） 三位数",
        "（5）中文括号无空格",
        "（4） 中文括号有空格",
        "（4) 一半中文括号，一半英文括号"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1. Title",
        "1) Title",
        "(一) Title",
        "Title (1)"
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_PARENTHESIZED_NUMBER.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_PARENTHESIZED_NUMBER.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }
}
