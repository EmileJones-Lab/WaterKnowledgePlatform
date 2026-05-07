package top.emilejones.hhu.preprocessing.structure.v1;

import top.emilejones.hhu.preprocessing.structure.MarkdownStructureExtractor;
import top.emilejones.hhu.preprocessing.structure.enums.TitleType;
import top.emilejones.hhu.preprocessing.structure.tree.Node;

/**
 * 通过将可能的标题扫描为结构树，从而根据结构树去规范标题结构的抽象类
 *
 * @author EmileJones
 */
public abstract class AbstractTitleTreeExtractor implements MarkdownStructureExtractor {
    /**
     * 对初始的文本进行预处理
     *
     * @return 返回处理过后的文本
     */
    protected abstract String initOriginText(String originText);

    /**
     * 判断一行文本是不是标题
     *
     * @param line 一行文本
     * @return 如果是标题则返回true，否则返回false
     */
    protected abstract boolean isTitle(String line);

    /**
     * 判断一行文本是不是一类标题的第一个，例如 "1", "一", "(1)"
     *
     * @param line 一行文本
     * @return 如果是一类标题的第一个标题则返回true，否则返回false
     */
    protected abstract boolean isFirstTitle(String line);

    /**
     * 提取出标题树结构
     *
     * @param originText 源文件的文本内容
     * @return 返回树的头节点，这个头节点必须是NilType类型
     */
    protected abstract Node extractStructureTree(String originText);

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
        if (!TitleType.NilType.equals(root.getTitleType()))
            throw new IllegalArgumentException("树的头节点不是NilType类型");
        return correctOriginTextByStructureTree(root);
    }
}
