package top.emilejones.hhu.splitter

import org.junit.jupiter.api.Test
import top.emilejones.hhu.spliter.impl.HtmlTableToCsvSplitter

class HtmlTableToCsvSplitterTest {

    @Test
    fun case1() {
        val text = """
            在宿县闸断面月下泄量满足要求的前提下，当团结闸断面下泄水量不满足相应年份月下泄量要求时，根据宿县闸、灵西闸蓄情况，及时调度加大泄量；当永城闸、宿县闸、符离集闸在满宿县闸断面下泄量要求前提下尚有较多蓄水时，可根据来预测加泄量；可适时核减新汴河流域宿县闸以下的下月取用水计划，超计划用份额在之后相邻的一个月或几个月内调整扣除。
        """.trimIndent()
        val result = HtmlTableToCsvSplitter.split(text, 1500)
        assert(result.isFailure)
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
        val result = HtmlTableToCsvSplitter.split(text, 1500)
        assert(result.isSuccess) {
            result.exceptionOrNull()?.message!!
        }
        val data = result.getOrThrow()
        assert(data[0].split("\n").size == 7) { "应有7行数据" }
    }
}