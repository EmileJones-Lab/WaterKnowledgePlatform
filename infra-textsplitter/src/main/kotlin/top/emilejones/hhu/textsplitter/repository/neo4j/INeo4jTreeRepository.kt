package top.emilejones.hhu.textsplitter.repository.neo4j

import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode

/**
 * Neo4j 树形结构仓库接口
 * 负责处理树状结构的构建以及基于图关系的遍历查询（父子、兄弟、前后节点）
 *
 * @author EmileJones
 */
interface INeo4jTreeRepository {

    /**
     * 按照树状结构插入
     *
     * @param rootNode 整个树状结构的根节点，这个根节点是空的头节点，它不会被插入。
     */
    fun insertTree(rootNode: TextNodeDTO)

    /**
     * 获取当前节点的下一个节点
     *
     * @param id 当前节点唯一标识
     * @return 下一个节点信息 (文件节点, 文本节点)
     */
    fun nextNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>?

    /**
     * 获取当前节点的前一个节点
     *
     * @param id 当前节点唯一标识
     * @return 前一个节点信息 (文件节点, 文本节点)
     */
    fun preNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>?

    /**
     * 获取当前节点的父亲节点的信息
     *
     * @param id 当前节点唯一标识
     * @return 父亲节点信息
     */
    fun parent(id: String): Neo4jTextNode?

    /**
     * 获取当前节点的兄弟节点，并且按照顺序排序
     *
     * @param id 当前节点唯一标识
     * @return 兄弟节点信息列表
     */
    fun siblings(id: String): List<Neo4jTextNode>

    /**
     * 获取当前节点的孩子节点
     *
     * @param id 当前节点唯一标识
     * @return 孩子节点信息列表
     */
    fun children(id: String): List<Neo4jTextNode>
}
