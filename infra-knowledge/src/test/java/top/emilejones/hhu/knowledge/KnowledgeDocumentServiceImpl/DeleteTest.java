package top.emilejones.hhu.knowledge.KnowledgeDocumentServiceImpl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeDocumentMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试 KnowledgeDocumentServiceImpl 的 delete 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试文档软删除功能。
     * 验证通过 update 操作将 isDelete 状态标记为已删除。
     */
    @Test
    public void delete() {
        String id = UUID.randomUUID().toString();
        ArgumentCaptor<KnowledgeDocumentDto> captor = ArgumentCaptor.forClass(KnowledgeDocumentDto.class);

        knowledgeDocumentService.delete(id);

        Mockito.verify(knowledgeDocumentMapper).update(captor.capture());
        assertEquals(id, captor.getValue().getDocumentId());
        assertEquals(DeleteConstant.DELETE, captor.getValue().getIsDelete());
    }
}
