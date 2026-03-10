package top.emilejones.hhu.knowledge.KnowledgeCatalogServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 测试 KnowledgeCatalogServiceImpl 的 findAll 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindAllTest {
    @Mock
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeCatalogServiceImpl knowledgeCatalogRepository;

    /**
     * 测试查询所有知识库目录的功能。
     * 验证 findAll 方法能够从 Mapper 获取所有数据，并正确过滤掉已标记为删除（isDelete = 0）的记录，
     * 最后将其余记录转换为领域对象。
     */
    @Test
    public void findAll() {
        KnowledgeCatalogDto dto1 = new KnowledgeCatalogDto();
        dto1.setKbId(UUID.randomUUID().toString());
        dto1.setIsDelete(1);
        dto1.setKbName("name1");
        dto1.setColName("col1");
        dto1.setCreateTime(Instant.now());
        dto1.setType(KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR);

        KnowledgeCatalogDto dto2 = new KnowledgeCatalogDto();
        dto2.setKbId(UUID.randomUUID().toString());
        dto2.setIsDelete(0); // 模拟已删除的数据
        dto2.setCreateTime(Instant.now());
        Mockito.when(knowledgeCatalogMapper.findAll()).thenReturn(Arrays.asList(dto1, dto2));

        List<KnowledgeCatalog> result = knowledgeCatalogRepository.findAll();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto1.getKbId(), result.get(0).getId());
    }
}
