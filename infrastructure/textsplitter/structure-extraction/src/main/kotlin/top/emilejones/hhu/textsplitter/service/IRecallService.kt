package top.emilejones.hhu.textsplitter.service

import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode

/**
 * 用来负责处理召回任务
 *
 * @author EmileJones
 */
interface IRecallService {

    /**
     * 召回和问题相关的节点
     *
     * @param query 问题
     * @param collectionName milvus的collection名称
     * @param filter 过滤条件
     * @return 和问题相关的节点
     */
    fun recallNode(query: String, collectionName: String, filter: String? = null): List<Neo4jTextNode>

    /**
     * 召回和问题相关的文件节点
     *
     * @param query 问题
     * @param collectionName milvus的collection名称
     * @return 和问题相关的文件节点
     */
    fun recallFileNode(query: String, collectionName: String): List<FileNode>
}
