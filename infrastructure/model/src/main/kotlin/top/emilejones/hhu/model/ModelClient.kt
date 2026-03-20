package top.emilejones.hhu.model

import top.emilejones.hhu.model.pojo.RerankResult

/**
 * 定义访问模型的接口
 * @author emilejones
 */
interface ModelClient {
    /**
     * 将文本embedding为向量
     *
     * @param text 文本
     * @return embedding vector
     */
    fun embedding(text: String): List<Float>

    /**
     * 将文本列表根据问题rerank
     *
     * @param query 问题
     * @param textList 文档列表
     * @return 重排序后的结果
     */
    fun rerank(query: String, textList: List<String>): List<RerankResult>

    /**
     * 通过LLM生成答案
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return LLM生成的结果
     */
    fun llm(systemPrompt: String, userPrompt: String): String
}