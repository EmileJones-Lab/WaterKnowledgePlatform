package top.emilejones.hhu.test.splitter

import org.junit.jupiter.api.Test
import top.emilejones.hhu.spliter.HtmlTableSplitter

class HtmlTableSplitterTest {
    @Test
    fun case1(){
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
        assert(blocks.isSuccess) {"This should be success"}
        assert(blocks.getOrThrow().size == 3) {"This should have 3 sequences, but it have ${blocks.getOrThrow().size}"}
    }
}