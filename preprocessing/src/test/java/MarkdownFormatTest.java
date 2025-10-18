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
public class MarkdownFormatTest {
    public static void main(String[] args) {
        try {
            MultiMarkdownFileSelectDelegate multiMarkdownFileSelectDelegate =
                    new MultiMarkdownFileSelectDelegate("D:\\out", "D:\\output1");
            multiMarkdownFileSelectDelegate.run();
            System.out.println("所有符合条件的 Markdown 文件已处理完成！");
        } catch (Exception e) {
            System.err.println("处理过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}