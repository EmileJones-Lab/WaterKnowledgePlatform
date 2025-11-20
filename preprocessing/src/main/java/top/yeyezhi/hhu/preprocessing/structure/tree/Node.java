package top.yeyezhi.hhu.preprocessing.structure.tree;

import top.yeyezhi.hhu.preprocessing.structure.enums.TitleType;

import java.util.List;

/**
 * 标题树的数据结构
 *
 * @author EmileJones
 */
public class Node {
    // 该标题在文本的第几行（下标从0开始）
    private final int index;
    // 该标题的类型
    private final TitleType titleType;
    // 该标题的孩子节点
    private List<Node> children;
    // 该标题的父亲节点
    private Node parent;

    public Node(int index, TitleType titleType) {
        this.index = index;
        this.titleType = titleType;
    }

    public TitleType getTitleType() {
        return titleType;
    }

    public int getIndex() {
        return index;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    private void appendChild(Node child) {
        children.add(child);
    }

    private Node getChild(int index) {
        return children.get(index);
    }

    private int childrenNumber() {
        return children.size();
    }
}
