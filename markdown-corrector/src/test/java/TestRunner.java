import top.emilejones.hhu.preprocessing.handler.MixedStructureCorrector4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Markdown格式转换测试类
 * 用于测试MixedStructureCorrector的功能
 */
public class TestRunner {
    public static void main(String[] args) {
        try {
            // 读取示例Markdown文件
            Path inputPath = Paths.get("C:\\Users\\byl\\Desktop\\调度规程md\\屯溪流域龙头水库4330127000125  14\\auto\\屯溪流域龙头水库4330127000125.md");
            String markdownContent = Files.readString(inputPath);


            // 使用MixedStructureCorrector处理内容
            MixedStructureCorrector4 corrector = new MixedStructureCorrector4();
            String processedContent = corrector.handle(markdownContent);


            // 将处理后的内容保存到新文件
            Path outputPath = Paths.get("C:\\Users\\byl\\Desktop\\调度规程md\\屯溪流域龙头水库4330127000125  14\\auto\\屯溪流域龙头水库4330127000125更正后.md");
            Files.writeString(outputPath, processedContent);

            System.out.println("\n处理完成，结果已保存到：" + outputPath);

        } catch (IOException e) {
            System.err.println("文件读写错误：" + e.getMessage());
        } catch (Exception e) {
            System.err.println("处理过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}