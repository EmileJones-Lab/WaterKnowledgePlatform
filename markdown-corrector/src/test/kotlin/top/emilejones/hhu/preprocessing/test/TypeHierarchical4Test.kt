package top.emilejones.hhu.preprocessing.test

import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeHierarchical4Test {

    private fun shouldMatchProvider() = listOf(
        "1.1.1.1 Title",
        "1.2.3.4 测试标题",
        "# 1.1.1.1 标题",
        "10.10.10.10 Long Title",
        "1.1.1.2. Title",
        "5.2.1.1. 10 year ago ...",
        "8.52.1.3标题",
        "8.1.5.1Title",
        "8.1.52.2.标题",
        "5.1.2.1.100年前",
        "8.1.5.122000years",
        "1.2.3.4.5 标题",
        "1.2.3.4.5标题",
        "2.2.1.10.100 Title"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1.1.1 Title",
        "1.1.1",
        "Title 1.1.1.1",
        "2.5.0. 50 Title",
        "2.2.1.100 Title",
        "2.2.1.100. Title",
        "5.2.1.100年前后",
        "5.2.1.2002年前后",
    )

    private fun firstTitleShouldMatchProvider() = listOf(
        "1.1.1.1 Title",
        "# 1.1.1.1 标题",
        "5.2.1.1. 10 year ago ...",
        "8.1.5.1Title",
        "5.1.2.1.100年前",
        "1.2.3.4.1 Title"
    )

    private fun firstTitleShouldNotMatchProvider() = listOf(
        "1.2.3.4 测试标题",
        "10.10.10.10 Long Title",
        "1.1.1.2. Title",
        "8.52.1.3标题",
        "8.1.52.2.标题",
        "2.2.1.10.100 Title"
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_4.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_4.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }

    @Test
    fun testFirstTitleShouldMatch() {
        firstTitleShouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_4.firstTitleRegex).matcher(input).matches(),
                "Expected first title to match: $input"
            )
        }
    }

    @Test
    fun testFirstTitleShouldNotMatch() {
        firstTitleShouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_HIERARCHICAL_4.firstTitleRegex).matcher(input).matches(),
                "Expected first title not to match: $input"
            )
        }
    }
}
