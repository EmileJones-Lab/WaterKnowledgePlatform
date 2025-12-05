package top.emilejones.hhu.document;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.document.SourceDocument;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本类仅做测试用，测试前需要在infra-document文件夹下下入config.yml
 * @author Yeyezhi
 * */
@SpringBootTest(classes = TestApplication.class)
public class MinIOSourceDocumentRepositoryTest {

    @Autowired
    private MinIOSourceDocumentRepository repo;

    @Test
    void testSpringInjection() {
        System.out.println("Repository 是否注入成功？ = " + (repo != null));
    }

    @Test
    void testFindSourceDocumentById() {
        String id = "282"; // 这里替换成数据库中实际存在的 ID

        Optional<SourceDocument> doc = repo.findSourceDocumentById(id);
        assertTrue(doc.isPresent());

        doc.ifPresent(d -> {
            System.out.println("文件名: " + d.getName());
            System.out.println("文件路径: " + d.getFilePath());
            System.out.println("类型: " + d.getType());
        });
    }

    @Test
    void testOpenContentById() throws Exception {

        // 关键：调用 openContent
        try (InputStream is = repo.openContent("/bamboo/2024/12/09/24b06a3f123445578ee8dcf78d01fef7.txt")) {

            byte[] bytes = is.readAllBytes();  // 读完整文件
            String content = new String(bytes, StandardCharsets.UTF_8);  // 按 UTF-8 解码成字符串

            // 取前 50 个字符（不超过文件长度）
            int previewLength = Math.min(50, content.length());
            String preview = content.substring(0, previewLength);

            System.out.println("文件前 " + previewLength + " 个字符：");
            System.out.println(preview);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
