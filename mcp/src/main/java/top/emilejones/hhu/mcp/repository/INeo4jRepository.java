package top.emilejones.hhu.mcp.repository;

import top.emilejones.hhu.mcp.entity.TextNode;

/**
 * 请求Neo4j数据库的借口规范
 *
 * @author EmileJones
 */
public interface INeo4jRepository {
    /**
     * 获取当前节点的下一个节点
     * @param elementId 当前节点唯一标识
     * @return 下一个节点信息
     */
    TextNode nextNode(String elementId);

    /**
     * 获取当前节点的前一个节点
     * @param elementId 当前节点唯一标识
     * @return 前一个节点信息
     */
    TextNode preNode(String elementId);
}
