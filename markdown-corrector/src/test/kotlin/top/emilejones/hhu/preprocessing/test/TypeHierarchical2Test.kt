package top.emilejones.hhu.preprocessing.test

import kotlin.test.Test
import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeHierarchical2Test {

    private fun shouldMatchProvider() = listOf(
        "1.1 Title",
        "1.2 测试标题",
        "# 1.1 标题",
        "10.10 Long Title",
        "1.10 Title"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1. Title",
        "1.1.1 Title",
        "1",
        "1.1",
        "Title 1.1",
        "1.100 Title"
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_2.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_2.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }
}
