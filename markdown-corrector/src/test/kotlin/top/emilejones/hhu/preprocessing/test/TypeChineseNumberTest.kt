package top.emilejones.hhu.preprocessing.test

import kotlin.test.Test
import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeChineseNumberTest {

    private fun shouldMatchProvider() = listOf(
        "一、标题",
        "二. 标题",
        "三． 标题",
        "四。 标题",
        "二十一、 复杂数字",
        "# 一、 标题",
        "一百零一. Title"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1. Title",
        "（一） Title",
        "一 Title",
        "Title 一、"
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_CHINESE_NUMBER.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_CHINESE_NUMBER.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }
}
