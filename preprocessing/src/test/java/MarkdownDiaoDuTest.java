import top.emilejones.hhu.preprocessing.handler.MarkdownStructureCorrector;
import top.emilejones.hhu.preprocessing.handler.MultiMarkdownFileSelectDelegate;
import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector2;
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Markdown格式转换测试类
 * 用于测试MarkdownStructureCorrector的功能
 */
public class MarkdownDiaoDuTest {
    public static void main(String[] args) {
        try {
            MultiMarkdownFileSelectDelegate multiMarkdownFileSelectDelegate =
                    new MultiMarkdownFileSelectDelegate("C:\\Users\\byl\\Desktop\\调度规程md", "C:\\Users\\byl\\Desktop\\调度规程md_output");
            multiMarkdownFileSelectDelegate.run();
            System.out.println("所有符合条件的 Markdown 文件已处理完成！");
        } catch (Exception e) {
            System.err.println("处理过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}