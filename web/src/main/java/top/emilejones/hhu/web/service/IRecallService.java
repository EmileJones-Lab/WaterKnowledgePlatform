package top.emilejones.hhu.web.service;

import kotlin.Pair;
import top.emilejones.hhu.web.entity.FileNode;
import top.emilejones.hhu.web.entity.TextNode;

import java.util.List;

/**
 * 用来负责处理召回任务
 *
 * @author EmileJones
 */
public interface IRecallService {
    /**
     * 召回和问题相关的文本
     *
     * @param query 问题
     * @return 和问题相关的文本
     */
    List<String> recallText(String query);

    /**
     * 召回和问题相关的节点
     *
     * @param query 问题
     * @return 和问题相关的节点
     */
    List<Pair<FileNode, TextNode>> recallNode(String query);
}
