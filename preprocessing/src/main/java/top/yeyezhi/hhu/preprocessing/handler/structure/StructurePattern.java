package top.yeyezhi.hhu.preprocessing.handler.structure;

import java.util.List;
import java.util.regex.Pattern;

/**
 *  作用：封装单个文档结构模式（Pattern），用于描述一种标题编号结构
 *       包含：
 *         - 模式ID（id）
 *         - 对应的处理器类名（handler）
 *         - 多个用于识别该结构的正则表达式（compiledRules）
 *  使用场景：
 *       由 StructurePatternLoader 读取 YAML 配置文件后生成该对象，
 *       由 MarkdownStructureClassifier 用来匹配 Markdown 文档结构。
 *  @author yeyezhi
 */
public class StructurePattern {
    /** 模式编号，对应 YAML 中的 id 字段 */
    private int id;
    /** 对应处理器类名，如 MarkdownStructureCorrector3 */
    private String handler;
    /** 编译后的正则表达式集合，用于匹配文档标题样式 */
    private List<Pattern> compiledRules;

    /**
     * 构造函数：将正则字符串转换为 Pattern 对象
     *
     * @param id 模式编号
     * @param handler 对应处理器类名
     * @param rules 识别该结构的正则表达式字符串列表
     */
    public StructurePattern(int id, String handler, List<String> rules) {
        this.id = id;
        this.handler = handler;
        this.compiledRules = rules.stream().map(Pattern::compile).toList();
    }

    public int getId() { return id; }
    public String getHandler() { return handler; }
    public List<Pattern> getCompiledRules() { return compiledRules; }
}
