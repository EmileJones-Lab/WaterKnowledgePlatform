package top.emilejones.hhu.preprocessing.test

import kotlin.test.Test
import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeChineseParenthesizedNumberTest {

    private fun shouldMatchProvider() = listOf(
        "（一） 标题",
        "（二）标题",
        "（二十） 标题",
        "# （一） 标题"
    )

    private fun shouldNotMatchProvider() = listOf(
        "(1) Title",
        "一、 Title",
        "Title （一）",
        "(一) 英文括号" // Current regex seems to expect Chinese parentheses specifically: ^(?:#+\s*)?（([一二三四五六七八九十]+)）\s*(.+)$
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_CHINESE_PARENTHESIZED_NUMBER.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_CHINESE_PARENTHESIZED_NUMBER.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }
}
