package top.emilejones.hhu.textsplitter.preprocessor.spliter

/**
 * 切割文本的策略
 * @author EmileJones
 */
interface StringSplitter {
    fun split(text: String, maxSequenceLength: Int): Result<List<String>>
}