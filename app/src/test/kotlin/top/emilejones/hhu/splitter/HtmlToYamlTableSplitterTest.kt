package top.emilejones.hhu.splitter

import org.junit.jupiter.api.Test
import top.emilejones.hhu.spliter.HtmlToYamlTableSplitter

class HtmlToYamlTableSplitterTest {

    @Test
    fun case1() {
        val text = """
            <table>
                <tr>
                    <td>省</td>
                    <td>地市</td>
                    <td>行政区面积</td>
                </tr>
                <tr>
                    <td rowspan="2">河南</td>
                    <td>商丘</td>
                    <td>2035</td>
                </tr>
                <tr>
                    <td>永城</td>
                    <td>997</td>
                </tr>
                <tr>
                    <td rowspan="2">安徽</td>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td></td>
                    <td>2484</td>
                </tr>
                <tr>
                    <td>江苏</td>
                    <td>徐州</td>
                    <td>105</td>
                </tr>
                <tr>
                    <td colspan="2">合计</td>
                    <td>6692</td>
                </tr>
            </table>
        """.trimIndent()
        val result = HtmlToYamlTableSplitter.split(text, 30)
        assert(result.isSuccess) {
            result.exceptionOrNull()?.message!!
        }
        val data = result.getOrThrow()
        assert(data.size == 6) { "应有6行数据" }
    }

    @Test
    fun case2() {
        val text = """
            <table>
                <tr>
                    <td>省</td>
                    <td>地市</td>
                    <td>行政区面积</td>
                </tr>
                <tr>
                    <td rowspan="2">河南</td>
                    <td>商丘</td>
                    <td>2035</td>
                </tr>
                <tr>
                    <td>永城</td>
                    <td>997</td>
                </tr>
                <tr>
                    <td rowspan="2">安徽</td>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td></td>
                    <td>2484</td>
                </tr>
                <tr>
                    <td>江苏</td>
                    <td>徐州</td>
                    <td>105</td>
                </tr>
                <tr>
                    <td colspan="2">合计</td>
                    <td>6692</td>
                </tr>
            </table>
        """.trimIndent()
        val result = HtmlToYamlTableSplitter.split(text, 15)
        assert(result.isSuccess) {
            result.exceptionOrNull()?.message!!
        }
        val data = result.getOrThrow()
        assert(data.size == 6) { "A line should not be split" }
    }

}