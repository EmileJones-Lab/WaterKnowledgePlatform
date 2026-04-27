package top.emilejones.hhu.application.platform;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.application.platform.dto.LazyPageDTO;
import top.emilejones.hhu.application.platform.dto.knowledge.CandidateKnowledgeFileDTO;
import top.emilejones.hhu.application.platform.dto.knowledge.KnowledgeDirectoryDTO;
import top.emilejones.hhu.application.platform.dto.knowledge.KnowledgeFileDTO;
import top.emilejones.hhu.application.platform.dto.knowledge.request.AddKnowledgeDirectoryDTO;
import top.emilejones.hhu.application.platform.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.platform.utils.DtoConverter;
import top.emilejones.hhu.application.platform.utils.ListDtoConverter;
import top.emilejones.hhu.common.exception.ConflictException;
import top.emilejones.hhu.common.exception.DomainInvariantBrokenException;
import top.emilejones.hhu.common.exception.InternalAppException;
import top.emilejones.hhu.common.exception.NotFoundException;
import top.emilejones.hhu.common.exception.NotImplementedBusinessException;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.repository.KnowledgeCatalogRepository;
import top.emilejones.hhu.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.knowledge.repository.dto.KnowledgeDocumentWithBindTime;
import top.emilejones.hhu.domain.knowledge.service.KnowledgeDomainService;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMissionResult;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.repository.*;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.result.FileNode;
import top.emilejones.hhu.domain.result.TextNode;

import java.time.Instant;
import java.util.*;

/**
 * 知识库应用服务 V2 - 优化了可读性与结构
 * 保持原有业务逻辑（包括 N+1 查询与伪分页逻辑），主要通过方法提取提升代码清晰度。
 */
@Service
public class KnowledgeApplicationServiceV2 {
    private final ApplicationEventPublisher publisher;
    private final KnowledgeCatalogRepository knowledgeCatalogRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final OcrMissionRepository ocrMissionRepository;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final KnowledgeDomainService knowledgeDomainService;
    private final NodeRepository nodeRepository;
    private final TextNodeVectorRepository textNodeVectorRepository;

    public KnowledgeApplicationServiceV2(ApplicationEventPublisher publisher,
                                         KnowledgeCatalogRepository knowledgeCatalogRepository,
                                         KnowledgeDocumentRepository knowledgeDocumentRepository,
                                         EmbeddingMissionRepository embeddingMissionRepository,
                                         OcrMissionRepository ocrMissionRepository,
                                         StructureExtractionMissionRepository structureExtractionMissionRepository,
                                         KnowledgeDomainService knowledgeDomainService,
                                         NodeRepository nodeRepository,
                                         TextNodeVectorRepository textNodeVectorRepository) {
        this.publisher = publisher;
        this.knowledgeCatalogRepository = knowledgeCatalogRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.ocrMissionRepository = ocrMissionRepository;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.knowledgeDomainService = knowledgeDomainService;
        this.nodeRepository = nodeRepository;
        this.textNodeVectorRepository = textNodeVectorRepository;
    }

    /**
     * 获取知识库列表
     */
    @Transactional(readOnly = true)
    public List<KnowledgeDirectoryDTO> getAllKnowledgeDirectories() {
        return knowledgeCatalogRepository.findAll().stream()
                .map(DtoConverter::toKnowledgeDirectoryDTO)
                .toList();
    }

    /**
     * 获取结构化的知识库
     */
    @Transactional(readOnly = true)
    public List<KnowledgeDirectoryDTO> getStructuredKnowledgeDirectories() {
        return knowledgeCatalogRepository.findAll().stream()
                .filter(it -> KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR.equals(it.getType()))
                .map(DtoConverter::toKnowledgeDirectoryDTO)
                .toList();
    }

    /**
     * 新增一个知识库
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDirectoryDTO addKnowledgeDirectory(AddKnowledgeDirectoryDTO request) {
        String milvusCollectionName = "_" + UUID.randomUUID().toString().replace("-", "_");

        KnowledgeCatalog knowledgeCatalog = KnowledgeCatalog.Companion.create(
                request.getDirName(),
                milvusCollectionName,
                DtoConverter.mapKnowledgeDirectoryDTOType(request.getType())
        );

        knowledgeCatalogRepository.save(knowledgeCatalog);
        knowledgeCatalog.pushEvents().forEach(publisher::publishEvent);
        textNodeVectorRepository.createCollection(milvusCollectionName).getOrThrow();

        return DtoConverter.toKnowledgeDirectoryDTO(knowledgeCatalog);
    }

    /**
     * 修改一个知识库元信息
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDirectoryDTO updateKnowledgeDirectory(String id, String dirName) {
        KnowledgeCatalog knowledgeCatalog = checkAndGetCatalog(id);

        KnowledgeCatalog updateKnowledgeCatalog = knowledgeCatalog.copy(
                knowledgeCatalog.getId(),
                dirName,
                knowledgeCatalog.getMilvusCollectionName(),
                knowledgeCatalog.getCreateTime(),
                knowledgeCatalog.getType()
        );

        knowledgeCatalogRepository.save(updateKnowledgeCatalog);
        return DtoConverter.toKnowledgeDirectoryDTO(updateKnowledgeCatalog);
    }

    /**
     * 删除一个知识库
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeDirectory(String id) {
        KnowledgeCatalog catalog = checkAndGetCatalog(id);
        List<KnowledgeDocumentWithBindTime> boundDocs = knowledgeDocumentRepository.findDocumentsWithBindInfoByCatalogId(id, Integer.MAX_VALUE, 0, null);

        List<String> docIds = boundDocs.stream()
                .map(it -> it.getKnowledgeDocument().getId())
                .toList();

        List<String> fileNodeIds = findFileNodeIdsByDocumentIds(docIds);

        // 执行删除与解绑逻辑
        knowledgeCatalogRepository.deleteKnowledgeDocumentFromKnowledgeCatalog(Objects.requireNonNull(catalog.getId()), docIds);
        textNodeVectorRepository.deleteTextNodeFromVectorDatabases(fileNodeIds, Objects.requireNonNull(catalog.getMilvusCollectionName())).getOrThrow();
        knowledgeCatalogRepository.delete(id);
    }

    /**
     * 获取知识库中的知识文件详细信息列表
     */
    @Transactional(readOnly = true)
    public LazyPageDTO<KnowledgeFileDTO> getAllKnowledgeFileByDirId(String dirId, Integer limit, Integer pageNum, String keyword) {
        // 1. 校验
        KnowledgeCatalog catalog = checkAndGetCatalog(dirId);
        validateStructuredCatalog(catalog);

        // 2. 分页查询文档
        int offset = pageNum * limit;
        List<KnowledgeDocumentWithBindTime> boundDocs = knowledgeDocumentRepository
                .findDocumentsWithBindInfoByCatalogId(dirId, limit + 1, offset, keyword);

        // 3. 处理下一页逻辑
        boolean hasNextPage = false;
        if (boundDocs.size() > limit) {
            hasNextPage = true;
            boundDocs.remove(boundDocs.size() - 1);
        }

        // 4. 获取并验证关联的 Embedding 任务
        List<String> missionIds = boundDocs.stream()
                .map(it -> it.getKnowledgeDocument().getEmbeddingMissionId())
                .toList();
        List<EmbeddingMission> embeddingMissions = fetchAndValidateEmbeddingMissions(missionIds);

        // 5. 装配 DTO 列表
        List<KnowledgeFileDTO> dtoList = new ArrayList<>();
        for (int i = 0; i < boundDocs.size(); i++) {
            String sourceDocId = embeddingMissions.get(i).getSourceDocumentId();
            MissionContext context = fetchMissionContext(sourceDocId);

            KnowledgeFileDTO dto = DtoConverter.toKnowledgeFileDTO(
                    boundDocs.get(i),
                    context.ocrMissions(),
                    context.splitMissions(),
                    context.embeddingMissions()
            );
            dtoList.add(dto);
        }

        return new LazyPageDTO<>(hasNextPage, dtoList);
    }

    /**
     * 向知识库中添加一个知识文件
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeFileDTO addKnowledgeFileByDirId(String dirId, String documentId) {
        KnowledgeDocument doc = checkAndGetKnowledgeDocument(documentId);
        KnowledgeCatalog catalog = checkAndGetCatalog(dirId);

        // 1. 领域服务执行绑定校验并获取绑定时间
        Instant bindTime = knowledgeDomainService.bindKnowledgeDocumentToKnowledgeCatalog(doc, catalog);
        knowledgeCatalogRepository.bind(doc, catalog, bindTime);

        // 2. 获取向量化任务信息
        EmbeddingMission embeddingMission = embeddingMissionRepository.find(doc.getEmbeddingMissionId());
        if (embeddingMission == null) {
            throw new ConflictException(String.format("此知识文件 [%s] 不存在对应的 EmbeddingMission。", doc.getId()));
        }

        // 3. 将向量化数据存入向量数据库
        if (!embeddingMission.isSuccess())
            throw new DomainInvariantBrokenException(String.format("此知识文件 [%s] 不存在成功的 EmbeddingMission。", doc.getId()));
        String fileNodeId = embeddingMission.getSuccessResult().getFileNodeId();


        textNodeVectorRepository.saveTextNodeToVectorDatabase(List.of(fileNodeId), catalog.getMilvusCollectionName()).getOrThrow();


        // 4. 获取任务背景信息并封装 DTO
        MissionContext context = fetchMissionContext(embeddingMission.getSourceDocumentId());
        KnowledgeFileDTO dto = new KnowledgeFileDTO();
        dto.setId(doc.getId());
        dto.setType(DtoConverter.mapKnowledgeDocumentType(doc.getType()));
        dto.setBindTime(bindTime);
        dto.setOcrMission(context.ocrMissions());
        dto.setExtractStructureMission(context.splitMissions());
        dto.setEmbeddingMission(context.embeddingMissions());

        return dto;
    }

    /**
     * 删除知识库中的知识文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeFileByDirId(String dirId, List<String> documentIds) {
        KnowledgeCatalog catalog = checkAndGetCatalog(dirId);
        List<String> fileNodeIds = findFileNodeIdsByDocumentIds(documentIds);

        textNodeVectorRepository.deleteTextNodeFromVectorDatabases(fileNodeIds, Objects.requireNonNull(catalog.getMilvusCollectionName())).getOrThrow();
        knowledgeCatalogRepository.deleteKnowledgeDocumentFromKnowledgeCatalog(dirId, documentIds);
    }

    /**
     * 获取可以加入到这个知识库中的文件列表
     */
    @Transactional(readOnly = true)
    public LazyPageDTO<CandidateKnowledgeFileDTO> getAllCandidateFiles(String dirId, Integer limit, Integer pageNum, String keyWord) {
        // 1. 校验
        KnowledgeCatalog catalog = checkAndGetCatalog(dirId);
        validateStructuredCatalog(catalog);

        // 2. 查询所有候选文档
        List<KnowledgeDocument> candidateDocs = knowledgeDocumentRepository.findCandidateKnowledgeDocumentKnowledgeCatalogId(dirId, keyWord);

        // 3. 获取并验证关联的 Embedding 任务
        List<String> missionIds = candidateDocs.stream().map(KnowledgeDocument::getEmbeddingMissionId).toList();
        List<EmbeddingMission> embeddingMissions = fetchAndValidateEmbeddingMissions(missionIds);

        // 4. 装配全部 DTO
        List<CandidateKnowledgeFileDTO> allDtos = new ArrayList<>();
        for (int i = 0; i < candidateDocs.size(); i++) {
            String sourceDocId = embeddingMissions.get(i).getSourceDocumentId();
            MissionContext context = fetchMissionContext(sourceDocId);

            CandidateKnowledgeFileDTO dto = DtoConverter.toCandidateKnowledgeFileDTO(
                    candidateDocs.get(i),
                    context.ocrMissions(),
                    context.splitMissions(),
                    context.embeddingMissions()
            );
            allDtos.add(dto);
        }

        // 5. 伪分页处理
        List<CandidateKnowledgeFileDTO> pagedDtos = performPseudoPagination(allDtos, pageNum, limit);

        return new LazyPageDTO<>(false, pagedDtos);
    }

    // ==========================================
    // 内部私有辅助方法
    // ==========================================

    private KnowledgeCatalog checkAndGetCatalog(String id) {
        KnowledgeCatalog catalog = knowledgeCatalogRepository.find(id);
        if (catalog == null) {
            throw new NotFoundException("当前知识库不存在！");
        }
        return catalog;
    }

    private KnowledgeDocument checkAndGetKnowledgeDocument(String id) {
        KnowledgeDocument knowledgeDocument = knowledgeDocumentRepository.find(id);
        if (knowledgeDocument == null) {
            throw new NotFoundException("当前知识文件不存在！");
        }
        return knowledgeDocument;
    }

    private void validateStructuredCatalog(KnowledgeCatalog catalog) {
        if (catalog.getType() != KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR) {
            throw new NotImplementedBusinessException("目前只支持查询结构化数据库的数据");
        }
    }

    /**
     * 批量获取并验证向量化任务是否存在
     */
    private List<EmbeddingMission> fetchAndValidateEmbeddingMissions(List<String> ids) {
        List<String> missingIds = new ArrayList<>();
        List<EmbeddingMission> missions = new ArrayList<>();

        for (String id : ids) {
            EmbeddingMission m = embeddingMissionRepository.find(id);
            if (m == null) {
                missingIds.add(id);
            } else {
                missions.add(m);
            }
        }

        if (!missingIds.isEmpty()) {
            throw new ConflictException("找不到下列的 EmbeddingMission：" + String.join(",", missingIds));
        }

        return missions;
    }

    /**
     * 获取源文件的所有任务 DTO 背景信息
     */
    private MissionContext fetchMissionContext(String sourceDocumentId) {
        List<OcrMission> ocrMissions = ocrMissionRepository.findBySourceDocumentId(sourceDocumentId);
        List<StructureExtractionMission> splitMissions = structureExtractionMissionRepository.findBySourceDocumentId(sourceDocumentId);
        List<EmbeddingMission> embeddingMissions = embeddingMissionRepository.findBySourceDocumentId(sourceDocumentId);

        return new MissionContext(
                ListDtoConverter.toOcrMissionDTOList(ocrMissions),
                ListDtoConverter.toDocumentSplittingMissionDTOList(splitMissions),
                ListDtoConverter.toEmbeddingMissionDTOList(embeddingMissions)
        );
    }

    /**
     * 根据文档 ID 列表获取关联的向量数据库文本节点 ID 列表
     */
    private List<String> findTextNodeIdsByDocumentIds(List<String> docIds) {
        return docIds.stream()
                .map(knowledgeDocumentRepository::find)
                .filter(Objects::nonNull)
                .map(KnowledgeDocument::getEmbeddingMissionId)
                .map(embeddingMissionRepository::find)
                .peek(m -> {
                    if (m == null)
                        throw new DomainInvariantBrokenException("一个知识文件不应该不存在对应的成功的向量化任务");
                })
                .map(EmbeddingMission::getSuccessResult)
                .map(EmbeddingMissionResult.Success::getFileNodeId)
                .map(nodeRepository::findFileNodeByFileNodeId)
                .peek(o -> {
                    if (o.isEmpty())
                        throw new DomainInvariantBrokenException("一个知识文件不应该不存在对应的文件结构图");
                })
                .map(Optional::get)
                .map(FileNode::getId)
                .map(nodeRepository::findTextNodeListByFileNodeId)
                .flatMap(List::stream)
                .map(TextNode::getId)
                .toList();
    }

    /**
     * 安全的伪分页处理
     */
    private <T> List<T> performPseudoPagination(List<T> allItems, int pageNum, int limit) {
        int start = pageNum * limit;
        if (start >= allItems.size()) {
            return Collections.emptyList();
        }
        int end = Math.min(start + limit, allItems.size());
        return allItems.subList(start, end);
    }

    /**
     * 内部 DTO 背景容器
     */
    private record MissionContext(
            List<OcrMissionDTO> ocrMissions,
            List<DocumentSplittingMissionDTO> splitMissions,
            List<EmbeddingMissionDTO> embeddingMissions
    ) {
    }

    /**
     * 根据知识文件 ID 列表解析出对应的 fileNodeId 列表。
     * <p>
     * 该方法只用于删除知识库、解绑知识文件等需要清理向量数据的场景。
     * 既然调用方已经走到“删除对应向量数据”这一步，说明每个知识文件都应该已经存在成功的向量化任务。
     * 如果这里查到的向量化任务不存在、未成功，或成功结果中缺少 fileNodeId，说明数据库状态或业务链路已经不一致，
     * 应当按内部错误处理，而不是静默跳过或按普通业务冲突处理。
     */
    private List<String> findFileNodeIdsByDocumentIds(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return List.of();
        }

        List<String> fileNodeIds = new ArrayList<>(documentIds.size());
        for (String documentId : documentIds) {
            KnowledgeDocument knowledgeDocument = checkAndGetKnowledgeDocument(documentId);

            EmbeddingMission embeddingMission = embeddingMissionRepository.find(knowledgeDocument.getEmbeddingMissionId());
            if (embeddingMission == null) {
                throw new NotFoundException("当前embeddingMission不存在！");
            }

            if (!embeddingMission.isSuccess()) {
                throw new DomainInvariantBrokenException("当前知识文件对应的向量化任务尚未成功，无法获取 fileNodeId");
            }

            String fileNodeId = embeddingMission.getSuccessResult().getFileNodeId();

            fileNodeIds.add(fileNodeId);
        }

        return fileNodeIds;
    }
}
