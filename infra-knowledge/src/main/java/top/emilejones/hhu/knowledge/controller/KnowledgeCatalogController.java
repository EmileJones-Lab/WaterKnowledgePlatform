package top.emilejones.hhu.knowledge.controller;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.List;

/**
 * 对知识库信息的相关处理
 * @author EmileNathon
 */
@Controller
@Slf4j
public class KnowledgeCatalogController {
    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;
    /**
     * 查询所有的知识库信息
     * @return List<KnowledgeCatalogRepository> 集合不可以为null，可以为空
     */
    public @NotNull List<KnowledgeCatalog> findAll(){
        log.info("查询所有的知识库信息");
        return knowledgeCatalogService.findAll();
    }

    /**
     * 根据id查询知识库信息
     * @param KnowledgeCatalogId
     * @return KnowledgeCatalog
     */
    public KnowledgeCatalog find(@NotNull String KnowledgeCatalogId){
        log.info("根据id查询知识库信息：{}", KnowledgeCatalogId);
        return knowledgeCatalogService.find(KnowledgeCatalogId);
    }

    /**
     * 新增一个知识库，如果已经存在就更新记录
     * @param knowledgeCatalog
     */
    public void save(@NotNull KnowledgeCatalog knowledgeCatalog){
        log.info("新增知识库：{}",knowledgeCatalog);
        knowledgeCatalogService.save(knowledgeCatalog);
    }

    /**
     * 将向量化后的文件与知识库绑定
     * @param knowledgeDocument
     * @param knowledgeCatalog
     */
    public void bind(@NotNull KnowledgeDocument knowledgeDocument, @NotNull KnowledgeCatalog knowledgeCatalog){
        log.info("将向量化后的文件与知识库绑定：{}{}", knowledgeDocument, knowledgeCatalog);
        knowledgeCatalogService.bind(knowledgeDocument, knowledgeCatalog);
    }
}
