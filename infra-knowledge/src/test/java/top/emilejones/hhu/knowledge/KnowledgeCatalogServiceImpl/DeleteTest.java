package top.emilejones.hhu.knowledge.KnowledgeCatalogServiceImpl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试 KnowledgeCatalogServiceImpl 的 delete 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {
    @Mock
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeCatalogServiceImpl knowledgeCatalogRepository;

    /**
     * 测试知识库目录的软删除功能。
     * 验证 delete 方法能够通过更新 DTO 的 isDelete 字段为删除状态，并调用 Mapper 的 update 方法实现软删除。
     */
    @Test
    public void delete() {
        String id = UUID.randomUUID().toString();
        ArgumentCaptor<KnowledgeCatalogDto> captor = ArgumentCaptor.forClass(KnowledgeCatalogDto.class);

        knowledgeCatalogRepository.delete(id);

        Mockito.verify(knowledgeCatalogMapper).update(captor.capture());
        assertEquals(id, captor.getValue().getKbId());
        assertEquals(DeleteConstant.DELETE, captor.getValue().getIsDelete());
    }
}
