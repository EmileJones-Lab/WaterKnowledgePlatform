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
import java.util.UUID;

/**
 * 测试 KnowledgeCatalogServiceImpl 的 find 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindTest {
    @Mock
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeCatalogServiceImpl knowledgeCatalogRepository;

    /**
     * 测试根据 ID 查询存在的知识库目录。
     * 验证当数据库中存在未删除的匹配记录时，方法能正确返回对应的领域对象。
     */
    @Test
    public void findExist() {
        String id = UUID.randomUUID().toString();
        KnowledgeCatalogDto dto = new KnowledgeCatalogDto();
        dto.setKbId(id);
        dto.setIsDelete(1);
        dto.setKbName("test_kb");
        dto.setColName("test_col");
        dto.setCreateTime(Instant.now());
        dto.setType(KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR);

        Mockito.when(knowledgeCatalogMapper.find(id)).thenReturn(dto);

        KnowledgeCatalog result = knowledgeCatalogRepository.find(id);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(id, result.getId());
    }

    /**
     * 测试查询不存在或已删除的知识库目录。
     * 验证当记录不存在或 isDelete 标志为 0 时，方法返回 null。
     */
    @Test
    public void findNotExistOrDeleted() {
        String id = UUID.randomUUID().toString();
        
        // 情况1: 记录不存在
        Mockito.when(knowledgeCatalogMapper.find(id)).thenReturn(null);
        Assertions.assertNull(knowledgeCatalogRepository.find(id));

        // 情况2: 记录已标记删除
        KnowledgeCatalogDto deletedDto = new KnowledgeCatalogDto();
        deletedDto.setIsDelete(0);
        Mockito.when(knowledgeCatalogMapper.find(id)).thenReturn(deletedDto);
        Assertions.assertNull(knowledgeCatalogRepository.find(id));
    }
}
