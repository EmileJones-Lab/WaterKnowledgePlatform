package top.emilejones.hhu.preprocessing.test

import kotlin.test.Test
import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeHierarchical3Test {

    private fun shouldMatchProvider() = listOf(
        "1.1.1 Title",
        "1.2.3 测试标题",
        "# 1.1.1 标题",
        "10.10.10 Long Title",
        "1.1.1. 标题",
        "5.2.1 100 title",
        "5.2.1. 100 title",
        "8.1.2标题",
        "8.2.5Title",
        "8.2.2.Title",
        "5.2.1.100年前后"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1.1 Title",
        "1.1.1.1 Title",
        "1.1",
        "Title 1.1.1",
        "100.100.100 Title",
        "5.2. 1 title",
        "8.2.52005years ago",
        "5.2.2.10years",
        "5.2.2.10years"
    )

    private fun firstTitleShouldMatchProvider() = listOf(
        "1.1.1 Title",
        "# 1.1.1 标题",
        "1.1.1. 标题",
        "5.2.1 100 title",
        "5.2.1. 100 title",
        "5.2.1.100年前后"
    )

    private fun firstTitleShouldNotMatchProvider() = listOf(
        "1.2.3 测试标题",
        "10.10.10 Long Title",
        "8.1.2标题",
        "8.2.5Title",
        "8.2.2.Title"
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_3.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_3.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }

    @Test
    fun testFirstTitleShouldMatch() {
        firstTitleShouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_3.firstTitleRegex).matcher(input).matches(),
                "Expected first title to match: $input"
            )
        }
    }

    @Test
    fun testFirstTitleShouldNotMatch() {
        firstTitleShouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_3.firstTitleRegex).matcher(input).matches(),
                "Expected first title not to match: $input"
            )
        }
    }
}
