package top.emilejones.hhu.knowledge.KnowledgeCatalogServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.UUID;

/**
 * 测试 KnowledgeCatalogServiceImpl 的 save 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Mock
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeCatalogServiceImpl knowledgeCatalogRepository;

    /**
     * 测试知识库目录的新增保存功能。
     * 验证当数据库中不存在对应知识库 ID 的记录时，save 方法能够正确封装 DTO 
     * 并调用 Mapper 的 save 方法进行数据持久化，同时检查各字段映射是否准确。
     */
    @Test
    public void saveNotExist() {
        final String KB_NAME = "kb_name";
        final String MILVUS_NAME = "milvusName";
        final KnowledgeCatalogType TYPE = KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR;
        KnowledgeCatalog knowledgeCatalog = KnowledgeCatalog.Companion.create(KB_NAME, MILVUS_NAME, TYPE);
        ArgumentCaptor<KnowledgeCatalogDto> captor = ArgumentCaptor.forClass(KnowledgeCatalogDto.class);
        knowledgeCatalogRepository.save(knowledgeCatalog);
        Mockito.verify(knowledgeCatalogMapper).save(captor.capture());

        KnowledgeCatalogDto value = captor.getValue();
        Assertions.assertEquals(0, value.getId());
        Assertions.assertEquals(knowledgeCatalog.getId(), value.getKbId());
        Assertions.assertEquals(KB_NAME, value.getKbName());
        Assertions.assertEquals(MILVUS_NAME, value.getColName());
        Assertions.assertEquals(TYPE, value.getType());
        Assertions.assertEquals(DeleteConstant.EXIST, value.getIsDelete());
    }

    /**
     * 测试知识库目录的更新保存功能。
     * 验证当数据库中已存在对应知识库 ID 的记录时，save 方法能够识别并转而
     * 调用 Mapper 的 update 方法执行更新操作，而非重复插入。
     */
    @Test
    public void saveExist() {
        final String KB_NAME = "kb_name";
        final String MILVUS_NAME = "milvusName";
        final KnowledgeCatalogType TYPE = KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR;
        KnowledgeCatalog knowledgeCatalog = KnowledgeCatalog.Companion.create(KB_NAME, MILVUS_NAME, TYPE);
        ArgumentCaptor<KnowledgeCatalogDto> captor = ArgumentCaptor.forClass(KnowledgeCatalogDto.class);
        Mockito.when(knowledgeCatalogMapper.find(knowledgeCatalog.getId())).thenReturn(new KnowledgeCatalogDto());
        knowledgeCatalogRepository.save(knowledgeCatalog);
        Mockito.verify(knowledgeCatalogMapper).update(captor.capture());
    }
}
