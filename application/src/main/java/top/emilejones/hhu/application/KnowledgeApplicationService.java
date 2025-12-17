package top.emilejones.hhu.application;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.application.dto.LazyPageDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeFileDTO;
import top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeFileDTO;
import top.emilejones.hhu.application.utils.DtoConverter;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeCatalogRepository;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.knowledge.infrastructure.dto.KnowledgeDocumentWithBindTime;
import top.emilejones.hhu.domain.pipeline.event.EmbeddingMissionSuccessEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class KnowledgeApplicationService {
    private final KnowledgeCatalogRepository knowledgeCatalogRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    /**
     * 构造函数。
     *
     * @param knowledgeCatalogRepository 知识目录仓储。
     */
    public KnowledgeApplicationService(
            KnowledgeCatalogRepository knowledgeCatalogRepository,
            KnowledgeDocumentRepository knowledgeDocumentRepository
    ) {
        this.knowledgeCatalogRepository = knowledgeCatalogRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    /**
     * 获取知识库列表
     *
     * @return 知识库列表
     */
    public List<KnowledgeDirectoryDTO> getAllKnowledgeDirectories() {
        // 1.获取所有的知识库信息
        List<KnowledgeCatalog> knowledgeCatalogList = knowledgeCatalogRepository.findAll();

        // 2.将knowledgeCatalog转换为KnowledgeDirectoryDTO
        List<KnowledgeDirectoryDTO> knowledgeDirectoryDTOList = knowledgeCatalogList.stream().map(DtoConverter::toKnowledgeDirectoryDTO).toList();

        // 3.返回数据
        return knowledgeDirectoryDTOList;
    }

    /**
     * 新增一个知识库
     *
     * @param request 新增请求
     * @return 新增的知识库元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDirectoryDTO addKnowledgeDirectory(AddKnowledgeDirectoryDTO request) {
        // 1.获取KnowledgeCatalog相关的信息
        // 1.1 用UUID获取kbId
        String kbId = UUID.randomUUID().toString();

        // 1.2 用UUID获取milvusCollectionName，并将 - 转成 _
        String milvusCollectionName = UUID.randomUUID().toString().replace("-", "_");

        // 2.将AddKnowledgeDirectoryDTO封装为KnowledgeCatalog
        KnowledgeCatalog knowledgeCatalog = new KnowledgeCatalog(
                kbId,
                request.getDirName(),
                milvusCollectionName,
                Instant.now(),
                DtoConverter.mapKnowledgeDirectoryDTOType(request.getType())
        );

        // 3.新增知识库
        knowledgeCatalogRepository.save(knowledgeCatalog);

        // 4.将KnowledgeCatalog信息封装为KnowledgeDirectoryDTO
        KnowledgeDirectoryDTO knowledgeDirectoryDTO = DtoConverter.toKnowledgeDirectoryDTO(knowledgeCatalog);

        // 5.返回相关信息
        return knowledgeDirectoryDTO;
    }

    /**
     * 修改一个知识库元信息
     *
     * @param id      知识库唯一Id
     * @param dirName 修改的知识库名称
     * @return 修改后的文件夹元信息
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDirectoryDTO updateKnowledgeDirectory(String id, String dirName) {
        // 1.根据id查询KnowledgeCatalog
        KnowledgeCatalog knowledgeCatalog = knowledgeCatalogRepository.find(id);

        // 如果不存在就报错
        if (knowledgeCatalog == null){
            throw new NullPointerException("当前知识库不存在！");
        }

        // 2.设置修改的属性
        KnowledgeCatalog updateKnowledgeCatalog = knowledgeCatalog.copy(knowledgeCatalog.getId(), dirName, knowledgeCatalog.getMilvusCollectionName(),
                knowledgeCatalog.getCreateTime(), knowledgeCatalog.getType());

        // 3.更新知识库信息
        knowledgeCatalogRepository.save(updateKnowledgeCatalog);

        // 4.将KnowledgeCatalog信息封装为KnowledgeDirectoryDTO
        KnowledgeDirectoryDTO knowledgeDirectoryDTO = DtoConverter.toKnowledgeDirectoryDTO(updateKnowledgeCatalog);

        // 5.返回知识库信息
        return knowledgeDirectoryDTO;
    }

    /**
     * 删除一个知识库
     *
     * @param id 知识库唯一Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeDirectory(String id) {
        //删除指定的知识库，这里是软删除只更新iddelete字段
        knowledgeCatalogRepository.delete(id);
    }

    /**
     * 获取知识库中的知识文件详细信息列表
     *
     * @param dirId   知识库唯一Id
     * @param limit   每页多少个数据
     * @param pageNum 第几页（从0开始）
     * @param keyword 根据文件名模糊匹配
     * @return 知识文件详细信息列表
     */
    public LazyPageDTO<KnowledgeFileDTO> getAllKnowledgeFileByDirId(String dirId, Integer limit, Integer pageNum, String keyword) {


        return null;
    }

    /**
     * 向知识库中添加一个知识文件
     *
     * @param dirId   知识库唯一Id
     * @param request 添加请求
     * @return 知识文件的元信息
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeFileDTO addKnowledgeFileByDirId(String dirId, AddKnowledgeFileDTO request) {
        return null;
    }

    /**
     * 删除知识库中的一个知识文件,实际是解绑的过程
     *
     * @param dirId       知识库唯一Id
     * @param embeddingId 知识文件唯一Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeFileByDirId(String dirId, String embeddingId) {

    }

    /**
     * 获取可以加入到这个知识库中的文件列表
     *
     * @param dirId 知识库唯一Id
     * @return 文件列表
     */
    public LazyPageDTO<KnowledgeFileDTO> getAllCandidateFiles(String dirId, String keyWord) {
        return null;
    }

    /**
     * 监听EmbeddingMissionSuccessEvent事件，将成功向量化后的文档添加到知识库。
     *
     * @param event 向量化任务成功事件
     */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void addAKnowledgeDocumentFromSuccessfulEmbeddingMission(EmbeddingMissionSuccessEvent event) {

    }
}
