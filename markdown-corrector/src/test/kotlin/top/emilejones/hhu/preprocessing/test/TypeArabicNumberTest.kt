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
        "5. 10 years ago"
    )

    private fun shouldNotMatchProvider() = listOf(
        "1.1 Title",
        "1.1.1 Title",
        "1.1",
        "1999. Year",
        "1",
        "1 ",
        "100. Title"
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
}
