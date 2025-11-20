package top.yeyezhi.hhu.preprocessing.structure;

/**
 * 提取正确Markdown结构的接口约束
 * @author EmileJones
 */
public interface MarkdownStructureExtractor {
    /**
     * 输入原本的文件，将其提取为层次结构正确的markdown文件
     * @param originText 源文件文本内容
     * @return 层次结构正确的markdown格式内容
     */
    String extract(String originText);
}
