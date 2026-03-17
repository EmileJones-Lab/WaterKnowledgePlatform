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
        "1.10 Title",
        "1.10. Title",
        "5.2 100 Title",
        "5.10. 100 year Title",
        "8.11.Title",
        "8.11标题",
        "8.12Title",
        "8.5.Title",
        "1.2.200年后",
        "2.1. 10年前",
        "1.2. 200年后",
        "2.1.2002年左右"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1. Title",
        "1.1.1 Title",
        "1",
        "1.1",
        "Title 1.1",
        "1.100 Title",
        "2. 5 Title",
        "8.12002years",
        "5.2.20years ago",
        "1.2.20年后",
    )

    private fun firstTitleShouldMatchProvider() = listOf(
        "1.1 Title",
        "# 1.1 标题",
        "10.1 Title",
        "1.1. Title",
        "2.1. 10年前",
        "2.1.2002年左右"
    )

    private fun firstTitleShouldNotMatchProvider() = listOf(
        "1.2 测试标题",
        "10.10 Long Title",
        "1.10 Title",
        "1.10. Title",
        "5.2 100 Title",
        "8.11.Title",
        "1.2.200年后"
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

    @Test
    fun testFirstTitleShouldMatch() {
        firstTitleShouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_2.firstTitleRegex).matcher(input).matches(),
                "Expected first title to match: $input"
            )
        }
    }

    @Test
    fun testFirstTitleShouldNotMatch() {
        firstTitleShouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_2.firstTitleRegex).matcher(input).matches(),
                "Expected first title not to match: $input"
            )
        }
    }
}
