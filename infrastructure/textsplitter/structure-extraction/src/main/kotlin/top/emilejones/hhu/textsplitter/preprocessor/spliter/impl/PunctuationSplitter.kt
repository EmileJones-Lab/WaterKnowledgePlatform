package top.emilejones.hhu.textsplitter.preprocessor.spliter.impl

import top.emilejones.hhu.textsplitter.preprocessor.spliter.StringSplitter

/**
 * 将句子按照标点符号切割的实现类
 * @author EmileJones
 */
object PunctuationSplitter : StringSplitter {
    private val punctuationRegex = Regex("[。；;\\s+]")
    private val lightPunctuationRegex = Regex("[，、]")

    /**
     * 按照标点符号切割的方法
     *
     * ## 拆分逻辑
     * 1. 拆分句子，如果一个文本长度大于maxSequenceLength，则拆分为每一个片段长度小于maxSequenceLength的多个片段。
     * 2. 首先根据“。；;”去切割，如果切割后的长度符合大小，则返回拆分后的结果。
     * 3. 如果根据“。；;”切割后的片段长度依然大于maxSequenceLength，则根据“，、”去拆分句子，如果切割后的长度符合大小，则返回拆分后的结果。
     * 4. 如果上述两种方式切割后依然无法满足要求的长度，直接返回Result.failure()。
     *
     * @param text 需要拆分的句子（最好不要有换行）
     * @param maxSequenceLength 每个片段的最大长度
     * @return 切分好的片段列表，如果切分失败则返回Result.failure()
     * @throws RuntimeException 当句子无法被拆分为指定的长度之内的片段的话报错
     */
    override fun split(text: String, maxSequenceLength: Int): Result<List<String>> {
        return try {
            val parts = splitAndKeepPunctuation(text)
            val result = assembleChunks(parts, maxSequenceLength)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 按标点切分并保留标点
     */
    private fun splitAndKeepPunctuation(text: String): List<String> {
        val sentences = punctuationRegex.split(text)
        val punctuations = punctuationRegex.findAll(text).map { it.value }.toList()
        return sentences.zip(punctuations)
            .map { (sentence, punctuation) -> (sentence + punctuation).trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * 将切分好的句子组合成尽量接近 maxSequenceLength 的块
     *
     * @param parts   已经按标点切分并保留标点的句子列表
     * @param maxLen  每个块的最大长度
     * @return        组合后的块列表
     */
    private fun assembleChunks(parts: List<String>, maxLen: Int): List<String> {
        val result = mutableListOf<String>()   // 存放最终结果
        val currentChunk = StringBuilder()     // 当前正在组装的块

        for (part in parts) {
            if (currentChunk.length + part.length <= maxLen) {
                // 如果加上这个句子还不超长，就直接加到当前块
                currentChunk.append(part)
                continue
            }

            // 如果当前块 + 新句子的长度超过了上限
            if (currentChunk.isNotEmpty()) {
                // 先把已经累积的 currentChunk 放到结果中
                result.add(currentChunk.toString())
                currentChunk.clear()
            }

            // 如果这个句子本身就比 maxLen 还长，说明需要拆分
            if (part.length > maxLen) {
                // 拆分成多个小块直接放到结果中
                result.addAll(splitLongSentence(part, maxLen))
            } else {
                // 否则直接作为新块的开头
                currentChunk.append(part)
            }


        }

        // 循环结束后，如果 currentChunk 里还有内容，放到结果中
        if (currentChunk.isNotEmpty()) {
            result.add(currentChunk.toString())
        }

        return result
    }


    /**
     * 拆分超过 maxLen 的长句
     * 拆分规则：
     *  1. 优先根据 `，` 或 `、` 拆成更小的子句
     *  2. 如果子句仍超过 maxLen，则再按字符长度截断
     */
    private fun splitLongSentence(sentence: String, maxLen: Int): List<String> {
        // 按轻量标点切分，同时保留这些标点
        val subSentences = splitAndKeepLightPunctuation(sentence)

        // 如果子句仍超长，则报错
        val illegalSentence = subSentences.find { it.length > maxLen }
        if (illegalSentence != null)
            throw RuntimeException("Can't split the sentence: $illegalSentence")

        return assembleChunks(subSentences, maxLen)
    }

    /**
     * 按 `，、` 轻量标点切分并保留标点
     */
    private fun splitAndKeepLightPunctuation(text: String): List<String> {
        val sentences = lightPunctuationRegex.split(text)
        val punctuations = lightPunctuationRegex.findAll(text).map { it.value }.toList()
        return sentences.zip(punctuations)
            .map { (s, p) -> (s + p).trim() }
            .filter { it.isNotEmpty() }
    }
}