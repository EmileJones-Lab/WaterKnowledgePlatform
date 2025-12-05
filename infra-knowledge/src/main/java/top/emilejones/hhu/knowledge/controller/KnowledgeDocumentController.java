package top.emilejones.hhu.knowledge.controller;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

@Controller
@Slf4j
public class KnowledgeDocumentController {
    @Autowired
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 新增向量化文件，如果存在就更新
     * @param knowledgeDocument
     */
    public void save(@NotNull KnowledgeDocument knowledgeDocument){
        log.info("新增向量化文件：{}", knowledgeDocument);
        knowledgeDocumentService.save(knowledgeDocument);
    }
}
