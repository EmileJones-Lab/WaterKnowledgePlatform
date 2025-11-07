package top.yeyezhi.hhu.preprocessing.handler.structure;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 *  作用：从 YAML 文件中加载所有文档结构模式配置
 *  功能说明：
 *      - 从 resources/config/structure_patterns.yml 中读取配置
 *      - 解析 patterns 数组
 *      - 将每个 pattern 转换为 StructurePattern 对象
 *  输出结果：
 *      - 返回 List<StructurePattern>，供 MarkdownStructureClassifier 使用
 *  注意事项：
 *      - 若文件未找到，会打印错误信息并返回空列表
 *  @author yeyezhi
 */
public class StructurePatternLoader {
    // ✅ 缓存已加载的 patterns，避免重复加载
    private static List<StructurePattern> cachedPatterns = null;
    /**
     * 从 YAML 文件加载所有结构模式配置
     *
     * @return 模式列表（List<StructurePattern>）
     */
    public static synchronized List<StructurePattern> loadPatterns() {
        // 如果已经加载过，直接返回缓存
        if (cachedPatterns != null && !cachedPatterns.isEmpty()) {
            return cachedPatterns;
        }

        try (InputStream input = StructurePatternLoader.class
                .getClassLoader()
                .getResourceAsStream("config/structure_patterns.yml")) {

            if (input == null) {
                System.err.println("❌ 找不到结构配置文件 structure_patterns.yml");
                return List.of();
            }

            System.out.println("🟡 正在尝试加载 YAML 配置...");

            Yaml yaml = new Yaml();
            Map<String, List<Map<String, Object>>> data = yaml.load(input);
            List<Map<String, Object>> patternsData = data.get("patterns");

            List<StructurePattern> patterns = new ArrayList<>();

            for (Map<String, Object> item : patternsData) {
                int id = (int) item.get("id");
                String handler = (String) item.get("handler");
                List<String> rules = (List<String>) item.get("rules");
                patterns.add(new StructurePattern(id, handler, rules));
                System.out.println("加载正则: " + rules);
            }
            cachedPatterns = patterns; // ✅ 缓存结果
            System.out.println("✅ 成功加载结构模板数量：" + patterns.size());
            return patterns;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
