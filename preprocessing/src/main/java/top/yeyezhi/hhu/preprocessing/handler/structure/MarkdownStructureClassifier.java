package top.yeyezhi.hhu.preprocessing.handler.structure;

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;
import top.yeyezhi.hhu.preprocessing.handler.*;

import java.util.*;
import java.util.regex.*;
/**
 *  作用：自动识别 Markdown 文档的结构类型并返回对应的处理器
 *  功能说明：
 *      1. 对传入的 Markdown 内容逐个匹配配置文件中的规则
 *      2. 统计每种模式匹配到的标题数量
 *      3. 匹配得分最高的模式即为最可能的结构类型
 *      4. 返回对应的处理器实例（MarkdownStructureCorrectorX 或 MixedStructureCorrectorX）
 *  依赖：
 *      - StructurePatternLoader：加载规则配置
 *      - StructurePattern：描述单个模式
 *  @author yeyezhi
 */
public class MarkdownStructureClassifier {
    private final List<StructurePattern> patterns;
    /**
     * 构造函数
     * @param patterns 从 YAML 加载的结构模式列表
     */
    public MarkdownStructureClassifier(List<StructurePattern> patterns) {
        this.patterns = patterns;
    }

    /**
     * 检测 Markdown 文本的结构模式
     *
     * @param content Markdown 文本内容
     * @return 匹配得分最高的 StructurePattern
     */
    public StructurePattern detect(String content) {
        int bestScore = 0;
        StructurePattern best = null;

        System.out.println("🧩 开始检测 Markdown 结构类型...");
        System.out.println("当前加载的结构模板数：" + patterns.size());
        System.out.println("🔍 文件前100字符: " + content.substring(0, Math.min(100, content.length())).replace("\n"," "));

        for (StructurePattern p : patterns) {
            int score = 0;
            for (Pattern rule : p.getCompiledRules()) {
                Matcher m = rule.matcher(content);
                int localCount = 0;
                while (m.find()) localCount++;
                System.out.println("结构 " + p.getId() + " 规则 " + rule.pattern() + " 命中次数: " + localCount);
                score += localCount;
            }
            System.out.println("结构 " + p.getId() + " 总命中次数: " + score);
            if (score > bestScore) {
                bestScore = score;
                best = p;
            }
        }


        if (best != null) {
            System.out.println("✅ 检测到最匹配结构: " + best.getId() + " (" + best.getHandler() + "), 命中次数: " + bestScore);
        } else {
            System.out.println("⚠️ 未匹配到任何结构类型！");
        }

        return best;
    }


    /**
     * 根据匹配到的模式，创建对应的处理器实例
     *
     * @param pattern 匹配到的模式
     * @return MarkdownFileHandler 实例
     */
    public MarkdownFileHandler getHandler(StructurePattern pattern) {
        if (pattern == null) return null;

        // 根据 handler 名称返回对应的类实例
        return switch (pattern.getHandler()) {
            case "MarkdownStructureCorrector2" -> new MarkdownStructureCorrector2();
            case "MarkdownStructureCorrector3" -> new MarkdownStructureCorrector3();
            case "MarkdownStructureCorrector4" -> new MarkdownStructureCorrector4();
            case "MarkdownStructureCorrector5" -> new MarkdownStructureCorrector5();
            case "MixedStructureCorrector" -> new MixedStructureCorrector();
            case "MixedStructureCorrector2" -> new MixedStructureCorrector2();
            case "MixedStructureCorrector3" -> new MixedStructureCorrector3();
            case "MixedStructureCorrector4" -> new MixedStructureCorrector4();
            default -> null;
        };
    }
}
