package top.emilejones.hhu.preprocessing.test

import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeArabicNumberTest {

    private fun shouldMatchProvider() = listOf(
        "1. Title",
        "1. 标题",
        "1、 标题",
        "10. 标题",
        "1 Title",
        "1 标题",
        "# 1. 标题",
        "1. Title with spaces",
        "5 10 years ago",
        "5. 10 years ago",
        "1.200years ago"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1.1 Title",
        "1.2Title",
        "1.20Title",
        "1.2.Title",
        "1.1.1 Title",
        "1.1",
        "1999. Year",
        "1",
        "1 ",
        "100. Title"
    )

    private fun firstTitleShouldMatchProvider() = listOf(
        "1. Title",
        "1. 标题",
        "1、 标题",
        "1 Title",
        "1 标题",
        "# 1. 标题",
        "1. Title with spaces",
        "1.200years ago"
    )

    private fun firstTitleShouldNotMatchProvider() = listOf(
        "2. Title",
        "2、 标题",
        "10. 标题",
        "5 10 years ago",
        "5. 10 years ago"
    )

    @Test
    fun testShouldMatch() {
        shouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_ARABIC_NUMBER.titleRegex).matcher(input).matches(),
                "Expected to match: $input"
            )
        }
    }

    @Test
    fun testShouldNotMatch() {
        shouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_ARABIC_NUMBER.titleRegex).matcher(input).matches(),
                "Expected not to match: $input"
            )
        }
    }

    @Test
    fun testFirstTitleShouldMatch() {
        firstTitleShouldMatchProvider().forEach { input ->
            assertTrue(
                Pattern.compile(TitleType.TYPE_ARABIC_NUMBER.firstTitleRegex).matcher(input).matches(),
                "Expected first title to match: $input"
            )
        }
    }

    @Test
    fun testFirstTitleShouldNotMatch() {
        firstTitleShouldNotMatchProvider().forEach { input ->
            assertFalse(
                Pattern.compile(TitleType.TYPE_ARABIC_NUMBER.firstTitleRegex).matcher(input).matches(),
                "Expected first title not to match: $input"
            )
        }
    }
}
