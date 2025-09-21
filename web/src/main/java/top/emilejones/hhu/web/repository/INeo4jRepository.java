package top.emilejones.hhu.web.repository;


import kotlin.Pair;
import top.emilejones.hhu.web.entity.FileNode;
import top.emilejones.hhu.web.entity.TextNode;

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
    Pair<FileNode, TextNode> nextNode(String elementId);

    /**
     * 获取当前节点的前一个节点
     * @param elementId 当前节点唯一标识
     * @return 前一个节点信息
     */
    Pair<FileNode, TextNode> preNode(String elementId);

    /**
     * 根据elementId获取节点的详细信息
     * @param elementId 节点Id
     * @return 节点的详细信息
     */
    Pair<FileNode, TextNode> selectByElementId(String elementId);
}
