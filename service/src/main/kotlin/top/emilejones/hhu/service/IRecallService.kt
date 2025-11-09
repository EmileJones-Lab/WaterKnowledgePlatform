package top.emilejones.hhu.service

import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jTextNode

/**
 * 用来负责处理召回任务
 *
 * @author EmileJones
 */
interface IRecallService {
    /**
     * 召回和问题相关的文本
     *
     * @param query 问题
     * @return 和问题相关的文本
     */
    fun recallText(query: String): List<String>

    /**
     * 召回和问题相关的节点
     *
     * @param query 问题
     * @return 和问题相关的节点
     */
    fun recallNode(query: String): List<Pair<Neo4jFileNode, Neo4jTextNode>>
}