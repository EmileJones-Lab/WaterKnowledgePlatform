package top.emilejones.hhu.spliter

interface StringSplitter {
    fun split(text: String, maxSequenceLength: Int): Result<List<String>>
}