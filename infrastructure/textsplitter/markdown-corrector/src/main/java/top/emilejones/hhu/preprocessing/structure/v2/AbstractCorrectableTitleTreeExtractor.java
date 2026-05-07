package top.emilejones.hhu.preprocessing.structure.v2;

import top.emilejones.hhu.preprocessing.structure.MarkdownStructureExtractor;
import top.emilejones.hhu.preprocessing.structure.enums.TitleType;
import top.emilejones.hhu.preprocessing.structure.tree.Node;

/**
 * 支持在提取结构树之后、修正原文之前，对树结构进行修正的抽象标题提取器。
 *
 * @author EmileJones
 */
public abstract class AbstractCorrectableTitleTreeExtractor implements MarkdownStructureExtractor {

    /**
     * 对初始的文本进行预处理
     *
     * @return 返回处理过后的文本
     */
    protected abstract String initOriginText(String originText);

    /**
     * 提取出标题树结构
     *
     * @param originText 源文件的文本内容
     * @return 返回树的头节点，这个头节点必须是NilType类型
     */
    protected abstract Node extractStructureTree(String originText);

    /**
     * 修正标题树结构。
     * 在 {@link #extractStructureTree} 构建出原始树后、
     * {@link #correctOriginTextByStructureTree} 生成最终文本前调用。
     *
     * @param root 标题树的根节点，根节点是NilType类型
     */
    protected abstract void correctorStructureTree(Node root);

    /**
     * 根据标题树的内容，将原有的文本变为符合markdown格式且层次正确的文本
     *
     * @param root 标题树的根节点，根节点是NilType类型
     * @return 正确格式和层次的markdown文本
     */
    protected abstract String correctOriginTextByStructureTree(Node root);

    @Override
    public String extract(String originText) {
        String processedText = initOriginText(originText);
        Node root = extractStructureTree(processedText);
        if (!TitleType.NilType.equals(root.getTitleType())) {
            throw new IllegalArgumentException("树的头节点不是NilType类型");
        }
        correctorStructureTree(root);
        return correctOriginTextByStructureTree(root);
    }
}
