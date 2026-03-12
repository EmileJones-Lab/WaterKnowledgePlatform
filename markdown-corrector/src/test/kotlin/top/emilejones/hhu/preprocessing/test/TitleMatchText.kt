package top.emilejones.hhu.preprocessing.test

import top.emilejones.hhu.preprocessing.structure.enums.TitleType
import kotlin.test.Test
import kotlin.test.assertEquals

class TitleMatchText {

    private val hierarchical4Examples = listOf(
        "1.1.1.1 标题内容",
        "10.11.12.13 标题内容",
        "# 1.1.1.1 标题内容",
        "## 1.1.1.1 标题内容",
        "5.2.1.10年前"
    )

    private val hierarchical3Examples = listOf(
        "1.1.1 标题内容",
        "1.1.1. 标题内容",
        "10.11.12 标题内容",
        "# 1.1.1 标题内容",
        "### 1.1.1. 标题内容",
        "5.2.1. 10年前",
        "5.2.1 10年前",
        "5.2.1.2002年前后"
    )

    private val hierarchical2Examples = listOf(
        "1.1 标题内容",
        "1.1. 标题内容",
        "10.11 标题内容",
        "# 1.1 标题内容",
        "## 1.1. 标题内容",
        "2.1 10年前",
        "2.1. 10年前",
        "2.1.100年前",
        "2.1.2002年左右",
        "1.1流域自然地理特性"
    )

    private val chineseNumberExamples = listOf(
        "一、标题内容",
        "二. 标题内容",
        "三．标题内容",
        "四。标题内容",
        "# 五、标题内容",
        "十一、标题内容",
        "二十、 标题内容"
    )

    private val parenthesizedNumberExamples = listOf(
        "(1) 标题内容",
        "（2）标题内容",
        "(10) 标题内容",
        "# (1) 标题内容"
    )

    private val chineseParenthesizedNumberExamples = listOf(
        "（一）标题内容",
        "（十一）标题内容",
        "# （二）标题内容"
    )

    private val arabicNumberExamples = listOf(
        "1. 标题内容",
        "2、标题内容",
        "3 标题内容",
        "10. 标题内容",
        "10、标题内容",
        "10 标题内容",
        "# 1. 标题内容",
        "20. 2002年前后",
        "2. 100年前",
        "1 流域概况"
    )

    private val numberParenExamples = listOf(
        "1）标题内容",
        "10）标题内容",
        "123）标题内容",
        "# 1）标题内容"
    )

    private fun assertOnlyMatches(expected: TitleType, line: String) {
        val matches = TitleType.values().filter { type ->
            type.titleRegex != null && line.matches(type.titleRegex.toRegex())
        }
        assertEquals(listOf(expected), matches, "Expected exactly one match for type $expected, but got $matches for: '$line'")
    }

    @Test
    fun testHierarchical4() {
        hierarchical4Examples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_HIERARCHICAL_4, line)
        }
    }

    @Test
    fun testHierarchical3() {
        hierarchical3Examples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_HIERARCHICAL_3, line)
        }
    }

    @Test
    fun testHierarchical2() {
        hierarchical2Examples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_HIERARCHICAL_2, line)
        }
    }

    @Test
    fun testChineseNumber() {
        chineseNumberExamples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_CHINESE_NUMBER, line)
        }
    }

    @Test
    fun testParenthesizedNumber() {
        parenthesizedNumberExamples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_PARENTHESIZED_NUMBER, line)
        }
    }

    @Test
    fun testChineseParenthesizedNumber() {
        chineseParenthesizedNumberExamples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_CHINESE_PARENTHESIZED_NUMBER, line)
        }
    }

    @Test
    fun testArabicNumber() {
        arabicNumberExamples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_ARABIC_NUMBER, line)
        }
    }

    @Test
    fun testNumberParen() {
        numberParenExamples.forEach { line ->
            assertOnlyMatches(TitleType.TYPE_NUMBER_PAREN, line)
        }
    }
}
