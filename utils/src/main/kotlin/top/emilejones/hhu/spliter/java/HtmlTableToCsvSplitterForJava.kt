package top.emilejones.hhu.spliter.java

import top.emilejones.hhu.spliter.HtmlTableToCsvSplitter

object HtmlTableToCsvSplitterForJava : StringSplitterForJava {
    override fun split(text: String, maxSequenceLength: Int): List<String> {
        return HtmlTableToCsvSplitter.split(text, maxSequenceLength).getOrThrow()
    }
}