package top.emilejones.hhu.splitter

import top.emilejones.hhu.textsplitter.spliter.impl.HtmlTableSplitter
import kotlin.test.Test

class HtmlTableSplitterTest {
    @Test
    fun case1() {
        val html = """
        <table>
            <tr><th>Name</th><th>Age</th></tr>
            <tr><td>Alice</td><td>20</td></tr>
            <tr><td>Bob</td><td>25</td></tr>
            <tr><td>Charlie</td><td>30</td></tr>
        </table>
    """.trimIndent()

        val splitter = HtmlTableSplitter
        val blocks = splitter.split(html, maxSequenceLength = 100)
        assert(blocks.isSuccess) { "This should be success" }
        assert(blocks.getOrThrow().size == 3) { "This should have 3 sequences, but it have ${blocks.getOrThrow().size}" }
    }

    @Test
    fun case2() {
        val text = """
            <table>
            <tr>
              <td>hi</td>
              <td>this is a longer cell with some random words inside</td>
              <td>abc123</td>
              <td>short</td>
            </tr>
            <tr>
              <td>example data here</td>
              <td>tiny</td>
              <td>this cell has about fifty characters of content in total right now</td>
              <td>ok</td>
            </tr>
            <tr>
              <td>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</td>
              <td>random text</td>
              <td>content with mixed length, sometimes short, sometimes long enough</td>
              <td>yo</td>
            </tr>
            <tr>
              <td>cell</td>
              <td>random filler words: apple banana cherry dragonfruit elderberry fig grape honeydew</td>
              <td>more data</td>
              <td>zz</td>
            </tr>
            <tr>
              <td>this cell is intentionally very long, with over ninety characters, so that we can reach the target size for the table quickly</td>
              <td>hi</td>
              <td>1234567890</td>
              <td>another medium sized cell here with some text</td>
            </tr>
            <tr>
              <td>ok</td>
              <td>medium cell here</td>
              <td>smol</td>
              <td>this is about seventy characters long, just enough for balance in table size</td>
            </tr>
            <tr>
              <td>text</td>
              <td>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</td>
              <td>bb</td>
              <td>some moderate data string here for demonstration</td>
            </tr>
            <tr>
              <td>shorty</td>
              <td>lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt</td>
              <td>ok</td>
              <td>hi again</td>
            </tr>
            <tr>
              <td>some row data</td>
              <td>small</td>
              <td>larger content with nearly one hundred characters so we fill the quota more effectively in this row</td>
              <td>end</td>
            </tr>
            <tr>
              <td>final</td>
              <td>another cell with some content, about forty chars</td>
              <td>xx</td>
              <td>done</td>
            </tr>
            <tr>
              <td>hi</td>
              <td>this is a longer cell with some random words inside</td>
              <td>abc123</td>
              <td>short</td>
            </tr>
            <tr>
              <td>example data here</td>
              <td>tiny</td>
              <td>this cell has about fifty characters of content in total right now</td>
              <td>ok</td>
            </tr>
            <tr>
              <td>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</td>
              <td>random text</td>
              <td>content with mixed length, sometimes short, sometimes long enough</td>
              <td>yo</td>
            </tr>
            <tr>
              <td>cell</td>
              <td>random filler words: apple banana cherry dragonfruit elderberry fig grape honeydew</td>
              <td>more data</td>
              <td>zz</td>
            </tr>
            <tr>
              <td>this cell is intentionally very long, with over ninety characters, so that we can reach the target size for the table quickly</td>
              <td>hi</td>
              <td>1234567890</td>
              <td>another medium sized cell here with some text</td>
            </tr>
            <tr>
              <td>ok</td>
              <td>medium cell here</td>
              <td>smol</td>
              <td>this is about seventy characters long, just enough for balance in table size</td>
            </tr>
            <tr>
              <td>text</td>
              <td>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</td>
              <td>bb</td>
              <td>some moderate data string here for demonstration</td>
            </tr>
            <tr>
              <td>shorty</td>
              <td>lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt</td>
              <td>ok</td>
              <td>hi again</td>
            </tr>
            <tr>
              <td>some row data</td>
              <td>small</td>
              <td>larger content with nearly one hundred characters so we fill the quota more effectively in this row</td>
              <td>end</td>
            </tr>
            <tr>
              <td>final</td>
              <td>another cell with some content, about forty chars</td>
              <td>xx</td>
              <td>done</td>
            </tr>
            <tr>
              <td>hi</td>
              <td>this is a longer cell with some random words inside</td>
              <td>abc123</td>
              <td>short</td>
            </tr>
            <tr>
              <td>example data here</td>
              <td>tiny</td>
              <td>this cell has about fifty characters of content in total right now</td>
              <td>ok</td>
            </tr>
            <tr>
              <td>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</td>
              <td>random text</td>
              <td>content with mixed length, sometimes short, sometimes long enough</td>
              <td>yo</td>
            </tr>
            <tr>
              <td>cell</td>
              <td>random filler words: apple banana cherry dragonfruit elderberry fig grape honeydew</td>
              <td>more data</td>
              <td>zz</td>
            </tr>
            <tr>
              <td>this cell is intentionally very long, with over ninety characters, so that we can reach the target size for the table quickly</td>
              <td>hi</td>
              <td>1234567890</td>
              <td>another medium sized cell here with some text</td>
            </tr>
            <tr>
              <td>ok</td>
              <td>medium cell here</td>
              <td>smol</td>
              <td>this is about seventy characters long, just enough for balance in table size</td>
            </tr>
            <tr>
              <td>text</td>
              <td>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</td>
              <td>bb</td>
              <td>some moderate data string here for demonstration</td>
            </tr>
            <tr>
              <td>shorty</td>
              <td>lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt</td>
              <td>ok</td>
              <td>hi again</td>
            </tr>
            <tr>
              <td>some row data</td>
              <td>small</td>
              <td>larger content with nearly one hundred characters so we fill the quota more effectively in this row</td>
              <td>end</td>
            </tr>
            <tr>
              <td>final</td>
              <td>another cell with some content, about forty chars</td>
              <td>xx</td>
              <td>done</td>
            </tr>
            <tr>
              <td>hi</td>
              <td>this is a longer cell with some random words inside</td>
              <td>abc123</td>
              <td>short</td>
            </tr>
            <tr>
              <td>example data here</td>
              <td>tiny</td>
              <td>this cell has about fifty characters of content in total right now</td>
              <td>ok</td>
            </tr>
            <tr>
              <td>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</td>
              <td>random text</td>
              <td>content with mixed length, sometimes short, sometimes long enough</td>
              <td>yo</td>
            </tr>
            <tr>
              <td>cell</td>
              <td>random filler words: apple banana cherry dragonfruit elderberry fig grape honeydew</td>
              <td>more data</td>
              <td>zz</td>
            </tr>
            <tr>
              <td>this cell is intentionally very long, with over ninety characters, so that we can reach the target size for the table quickly</td>
              <td>hi</td>
              <td>1234567890</td>
              <td>another medium sized cell here with some text</td>
            </tr>
            <tr>
              <td>ok</td>
              <td>medium cell here</td>
              <td>smol</td>
              <td>this is about seventy characters long, just enough for balance in table size</td>
            </tr>
            <tr>
              <td>text</td>
              <td>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</td>
              <td>bb</td>
              <td>some moderate data string here for demonstration</td>
            </tr>
            <tr>
              <td>shorty</td>
              <td>lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt</td>
              <td>ok</td>
              <td>hi again</td>
            </tr>
            <tr>
              <td>some row data</td>
              <td>small</td>
              <td>larger content with nearly one hundred characters so we fill the quota more effectively in this row</td>
              <td>end</td>
            </tr>
            <tr>
              <td>final</td>
              <td>another cell with some content, about forty chars</td>
              <td>xx</td>
              <td>done</td>
            </tr>
            </table>
        """.trimIndent()
        val result = HtmlTableSplitter.split(text, 700).getOrThrow()
        result.forEach { assert(it.length <= 700) { "Every table char number should less than 700" } }
    }

    @Test
    fun case3() {
        val text = """
            <table><tr><td rowspan="2" colspan="1">水库名称</td><td rowspan="1" colspan="7">汛期控制运用</td><td rowspan="2" colspan="1">控制运用办法</td><td rowspan="2" colspan="1">兴利水位（m)</td><td rowspan="2" colspan="1">工程存在问题</td><td rowspan="2" colspan="1">防御洪水措施</td><td rowspan="2" colspan="1">防汛责任人（职务）</td><td rowspan="2" colspan="1">(cid:)</td></tr><tr><td rowspan="1" colspan="1">朗口</td><td rowspan="1" colspan="1">5月1日到9月30日</td><td rowspan="1" colspan="1">197</td><td rowspan="1" colspan="1">57</td><td rowspan="1" colspan="1">201.98</td><td rowspan="1" colspan="1">314.69</td><td rowspan="1" colspan="1">199</td><td rowspan="1" colspan="1">83</td><td rowspan="1" colspan="1">1、汛期水位超过197m时，开启发电涵和底涵放水降低水位至197m 以下。2、汛期水位超过199m 时，自由溢流同时，满负荷发电，底涵开至最大。</td><td rowspan="1" colspan="1">199</td><td rowspan="1" colspan="1">(cid:)</td><td rowspan="1" colspan="1">1、组织30人抢险小分队，备足防汛抢险物资；2、汛期坚持24小时值班，严格控制汛期水位；3、预报有强降雨时，通过开启底涵提前放水降低水位至194m，腾空部分库容。4、预计发生超标准洪水时，水库防汛相关责任人迅速做出分析判断，提前启动水库防洪预案，发布洪水预报和警报，组织水库下游沿河影响区居民紧急转移。</td><td rowspan="1" colspan="1">汪景峰（副县长）方程（镇长）</td><td></td></tr></table>
        """.trimIndent()
        val result = HtmlTableSplitter.split(text, 700).getOrThrow()
        val count = result.filter { it.length > 700 }.count()
        assert(count > 0) { "A line should not be split" }
    }
}