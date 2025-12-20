package top.emilejones.hhu.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.application.dto.LazyPageDTO;
import top.emilejones.hhu.application.dto.knowledge.CandidateKnowledgeFileDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeFileDTO;
import top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.utils.DtoConverter;
import top.emilejones.hhu.application.utils.ListDtoConverter;
import top.emilejones.hhu.domain.document.infrastruction.SourceDocumentRepository;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.domain.knowledge.event.KnowledgeDocumentAddedToCatalogEvent;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeCatalogRepository;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.knowledge.infrastructure.dto.KnowledgeDocumentWithBindTime;
import top.emilejones.hhu.domain.knowledge.service.KnowledgeDomainService;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.event.EmbeddingMissionSuccessEvent;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class KnowledgeApplicationService {
    private final ApplicationEventPublisher publisher;
    private final KnowledgeCatalogRepository knowledgeCatalogRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final OcrMissionRepository ocrMissionRepository;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final KnowledgeDomainService knowledgeDomainService;

    /**
     * 构造函数。
     *
     * @param knowledgeCatalogRepository 知识目录仓储。
     */
    public KnowledgeApplicationService(
            KnowledgeCatalogRepository knowledgeCatalogRepository,
            KnowledgeDocumentRepository knowledgeDocumentRepository,
            ApplicationEventPublisher publisher,
            EmbeddingMissionRepository embeddingMissionRepository,
            OcrMissionRepository ocrMissionRepository,
            StructureExtractionMissionRepository structureExtractionMissionRepository,
            SourceDocumentRepository sourceDocumentRepository,
            KnowledgeDomainService knowledgeDomainService
    ) {
        this.knowledgeCatalogRepository = knowledgeCatalogRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.publisher = publisher;
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.ocrMissionRepository = ocrMissionRepository;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.knowledgeDomainService = knowledgeDomainService;
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
        String milvusCollectionName = "_" + UUID.randomUUID().toString().replace("-", "_");

        // 2.将AddKnowledgeDirectoryDTO封装为KnowledgeCatalog
        KnowledgeCatalog knowledgeCatalog = KnowledgeCatalog.Companion.create(
                kbId,
                request.getDirName(),
                milvusCollectionName,
                DtoConverter.mapKnowledgeDirectoryDTOType(request.getType())
        );

        // 3.新增知识库
        knowledgeCatalogRepository.save(knowledgeCatalog);
        knowledgeCatalog.pushEvents().forEach(publisher::publishEvent);

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
        if (knowledgeCatalog == null) {
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
        // 1.计算偏移量
        Integer offset = pageNum * limit;

        // 2.分页查询知识库中的向量化文件，这里需要多查一条用于判断是否存在下一页
        List<KnowledgeDocumentWithBindTime> knowledgeDocumentWithBindTimeList = knowledgeDocumentRepository
                .findDocumentsWithBindInfoByCatalogId(dirId, limit + 1, offset, keyword);

        // 3.判断是否存在下一页
        Boolean hasNextPage = false;
        if (knowledgeDocumentWithBindTimeList.size() > limit) {
            // 如果存在下一页就设置变量为ture，并移除最后一个数据
            hasNextPage = true;
            knowledgeDocumentWithBindTimeList.remove(knowledgeDocumentWithBindTimeList.size() - 1);
        }

        // 4.将id与embeddingMission绑定，用于获取查询为null的embeddingMission的id，报错给前端
        List<AbstractMap.SimpleEntry<String, EmbeddingMission>> missionEntries = knowledgeDocumentWithBindTimeList.stream()
                .map(knowledgeDocumentWithBindTime -> knowledgeDocumentWithBindTime.getKnowledgeDocument().getEmbeddingMissionId())
                .map(id -> new AbstractMap.SimpleEntry<>(id, embeddingMissionRepository.findById(id)))
                .toList();

        // 4.1遍历整个embeddingMissionList，如果存在null的embeddingMission就记录他们的id并报错给前端
        List<String> nullEmbeddingMissionIds = missionEntries.stream()
                .filter(e -> e.getValue() == null)
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();

        // 4.2判断当前的nullEmbeddingMissionIds是否为空
        if (!nullEmbeddingMissionIds.isEmpty()) {
            // 不为空报错
            throw new NullPointerException("找不到下列的EmbeddingMission：" + String.join(",", nullEmbeddingMissionIds));
        }

        // 5.获取EmbeddingMission，并封装成EmbeddingMissionDTO
        // 5.1获取成功的EmbeddingMission
        List<EmbeddingMission> embeddingMissions = missionEntries.stream()
                .map(AbstractMap.SimpleEntry::getValue)
                .toList();

        // 5.2根据fileId查询所有的embeddingMission，包括没成功的
        List<List<EmbeddingMission>> embeddingMissionList = embeddingMissions.stream()
                .map(EmbeddingMission::getSourceDocumentId)
                .map(embeddingMissionRepository::findBySourceDocumentId)
                .toList();

        // 5.3封装成EmbeddingMissionDTO
        List<List<EmbeddingMissionDTO>> embeddingMissionDTOList = embeddingMissionList.stream()
                .map(ListDtoConverter::toEmbeddingMissionDTOList)
                .toList();


        // 6.根据embeddingMissionId查询对应的fileId，再根据fileId查询所有OcrMission，包括没成功的,并封装成OcrMissionDTO
        // 6.1根据embeddingMissionId查询对应的fileId，再根据fileId查询所有OcrMission，包括没成功的
        List<List<OcrMission>> ocrMissionList = embeddingMissions.stream()
                .map(EmbeddingMission::getSourceDocumentId)
                .map(ocrMissionRepository::findBySourceDocumentId)
                .toList();

        // 6.2封装成OcrMissionDTO
        List<List<OcrMissionDTO>> ocrMissionDTOList = ocrMissionList.stream()
                .map(ListDtoConverter::toOcrMissionDTOList)
                .toList();

        // 7.根据embeddingMissionId查询对应的fileId,再根据查询到的fileId查询所有的extractStructureMission, 并封装成DocumentSplittingMissionDTO
        // 7.1根据embeddingMissionId查询对应的fileId,再根据查询到的fileId查询所有的extractStructureMissio，包括没成功的
        List<List<StructureExtractionMission>> extractStructureMissionList = embeddingMissions.stream()
                .map(EmbeddingMission::getSourceDocumentId)
                .map(structureExtractionMissionRepository::findBySourceDocumentId)
                .toList();

        // 7.2封装成DocumentSplittingMissionDTO
        List<List<DocumentSplittingMissionDTO>> documentSplittingMissionDTOList = extractStructureMissionList.stream()
                .map(ListDtoConverter::toDocumentSplittingMissionDTOList)
                .toList();


        // 8封装成KnowledgeFileDTO
        List<KnowledgeFileDTO> knowledgeFileDTOList = new ArrayList<>(limit);
        for (int i = 0; i < knowledgeDocumentWithBindTimeList.size(); i++) {
            KnowledgeFileDTO knowledgeFileDTO = DtoConverter.toKnowledgeFileDTO(
                    knowledgeDocumentWithBindTimeList.get(i),
                    ocrMissionDTOList.get(i),
                    documentSplittingMissionDTOList.get(i),
                    embeddingMissionDTOList.get(i)
            );
            knowledgeFileDTOList.add(knowledgeFileDTO);
        }

        // 封装成一个LazyPageDTO，并返回
        LazyPageDTO<KnowledgeFileDTO> knowledgeFileDTOLazyPageDTO = new LazyPageDTO<KnowledgeFileDTO>(hasNextPage, knowledgeFileDTOList);

        return knowledgeFileDTOLazyPageDTO;
    }

    /**
     * 向知识库中添加一个知识文件
     *
     * @param dirId      知识库唯一Id
     * @param documentId 向量化文件唯一Id
     * @return 知识文件的元信息
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeFileDTO addKnowledgeFileByDirId(String dirId, String documentId) {
        // 1.根据documentId查询向量化文件
        KnowledgeDocument knowledgeDocument = knowledgeDocumentRepository.findKnowledgeDocumentByKnowledgeDocumentId(documentId);

        // 2.根据dirId查询知识库
        KnowledgeCatalog knowledgeCatalog = knowledgeCatalogRepository.find(dirId);

        // 判空
        if (knowledgeCatalog == null) {
            throw new NullPointerException("当前知识库不存在！");
        }

        // 3.绑定之前发布绑定的事件，获得绑定事件
        KnowledgeDocumentAddedToCatalogEvent knowledgeDocumentAddedToCatalogEvent = knowledgeDomainService.bindKnowledgeDocumentToKnowledgeCatalog(knowledgeDocument, knowledgeCatalog);

        // 4.绑定
        knowledgeCatalogRepository.bind(knowledgeDocument, knowledgeCatalog, knowledgeDocumentAddedToCatalogEvent.getBindTime());

        // 4.根据embeddingMissionId查询embeddingMission
        EmbeddingMission embeddingMission = embeddingMissionRepository.findById(knowledgeDocument.getEmbeddingMissionId());

        // 判空
        if (embeddingMission == null) {
            throw new NullPointerException("当前embeddingMission不存在！");
        }

        // 5.根据fileId查询对应的ocrMission、extractStructureMission和embeddingMission
        // 5.1查询ocrMission
        List<OcrMission> ocrMissionList = ocrMissionRepository.findBySourceDocumentId(embeddingMission.getSourceDocumentId());

        // 5.2查询extractStructureMission
        List<StructureExtractionMission> structureExtractionMissionList = structureExtractionMissionRepository.findBySourceDocumentId(embeddingMission.getSourceDocumentId());

        // 5.3查询embeddingMission
        List<EmbeddingMission> embeddingMissionList = embeddingMissionRepository.findBySourceDocumentId(embeddingMission.getSourceDocumentId());

        // 6.将ocrMissionList、structureExtractionMissionList和embeddingMissionList转换成对应的DTO
        // 6.1将ocrMissionList转成ocrMissionDTOList
        List<OcrMissionDTO> ocrMissionDTOList = ListDtoConverter.toOcrMissionDTOList(ocrMissionList);

        // 6.2将structureExtractionMissionList转成documentSplittingMissionDTOList
        List<DocumentSplittingMissionDTO> documentSplittingMissionDTOList = ListDtoConverter.toDocumentSplittingMissionDTOList(structureExtractionMissionList);

        // 6.3将embeddingMissionList转成documentSplittingMissionDTOList
        List<EmbeddingMissionDTO> embeddingMissionDTOList = ListDtoConverter.toEmbeddingMissionDTOList(embeddingMissionList);

        // 5.封装成KnowledgeFileDTO
        KnowledgeFileDTO knowledgeFileDTO = new KnowledgeFileDTO();
        knowledgeFileDTO.setId(knowledgeDocument.getId());
        knowledgeFileDTO.setType(DtoConverter.mapKnowledgeDocumentType(knowledgeDocument.getType()));
        knowledgeFileDTO.setBindTime(knowledgeDocumentAddedToCatalogEvent.getBindTime());
        knowledgeFileDTO.setOcrMission(ocrMissionDTOList);
        knowledgeFileDTO.setExtractStructureMission(documentSplittingMissionDTOList);
        knowledgeFileDTO.setEmbeddingMission(embeddingMissionDTOList);

        publisher.publishEvent(knowledgeDocumentAddedToCatalogEvent);

        return knowledgeFileDTO;
    }

    /**
     * 删除知识库中的一个知识文件,实际是解绑的过程
     *
     * @param dirId       知识库唯一Id
     * @param documentIds 向量化文件Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeFileByDirId(String dirId, List<String> documentIds) {
        knowledgeCatalogRepository.deleteKnowledgeDocumentFromKnowledgeCatalog(dirId, documentIds);
    }

    /**
     * 获取可以加入到这个知识库中的文件列表
     *
     * @param dirId 知识库唯一Id
     * @return 文件列表
     */
    public LazyPageDTO<CandidateKnowledgeFileDTO> getAllCandidateFiles(String dirId, Integer limit, Integer pageNum, String keyWord) {
        // 1.查询所有候选向量化文件
        List<KnowledgeDocument> knowledgeDocumentList = knowledgeDocumentRepository.findCandidateKnowledgeDocumentKnowledgeCatalogId(dirId, keyWord);

        // 2.根据embeddingMissionId查询对应的embeddingMission并和其id绑定，用于判断是否存在null
        List<AbstractMap.SimpleEntry<String, EmbeddingMission>> entries = knowledgeDocumentList.stream()
                .map(KnowledgeDocument::getEmbeddingMissionId)
                .map(id -> new AbstractMap.SimpleEntry<>(id, embeddingMissionRepository.findById(id)))
                .toList();

        // 3.获取找不到embeddingMission的id
        List<String> nullEmbeddingMissionIds = entries.stream()
                .filter(e -> e.getValue() == null)
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();

        // 4.判断当前nullEmbeddingMissionIds是否为空
        if (!nullEmbeddingMissionIds.isEmpty()) {
            // 不空就报错
            throw new NullPointerException("找不到下列的EmbeddingMission：" + String.join(",", nullEmbeddingMissionIds));
        }

        // 5.获取所有成功的embeddingMission
        List<EmbeddingMission> embeddingMissions = entries.stream()
                .map(AbstractMap.SimpleEntry::getValue)
                .toList();

        // 6.根据fileId查询所有的ocrMission包括不成功的，并封装成ocrMissionDTO
        // 6.1根据fileId查询所有的ocrMission包括不成功的
        List<List<OcrMission>> ocrMissionList = embeddingMissions.stream()
                .map(EmbeddingMission::getSourceDocumentId)
                .map(ocrMissionRepository::findBySourceDocumentId)
                .toList();

        // 6.2封装成ocrMissionDTO
        List<List<OcrMissionDTO>> ocrMissionDTOList = ocrMissionList.stream()
                .map(ListDtoConverter::toOcrMissionDTOList)
                .toList();

        // 7.根据fileId查询所有的extractStructureMission包括不成功的，并封装成extractStructureMissionDTO
        // 7.1根据fileId查询所有的extractStructureMission包括不成功的
        List<List<StructureExtractionMission>> structureExtractionMissionList = embeddingMissions.stream()
                .map(EmbeddingMission::getSourceDocumentId)
                .map(structureExtractionMissionRepository::findBySourceDocumentId)
                .toList();

        // 7.2封装成extractStructureMissionDTO
        List<List<DocumentSplittingMissionDTO>> documentSplittingMissionDTOList = structureExtractionMissionList.stream()
                .map(ListDtoConverter::toDocumentSplittingMissionDTOList)
                .toList();

        // 8.根据fileId查询所有的embeddingMission包括不成功的，并封装成embeddingMissionDTO
        // 8.1根据fileId查询所有的embeddingMission包括不成功的
        List<List<EmbeddingMission>> embeddingMissionList = embeddingMissions.stream()
                .map(EmbeddingMission::getSourceDocumentId)
                .map(embeddingMissionRepository::findBySourceDocumentId)
                .toList();

        // 8.2封装成embeddingMissionDTO
        List<List<EmbeddingMissionDTO>> embeddingMissionDTOList = embeddingMissionList.stream()
                .map(ListDtoConverter::toEmbeddingMissionDTOList)
                .toList();

        // 9.将所有信息封装为CandidateKnowledgeFileDTOList
        List<CandidateKnowledgeFileDTO> candidateKnowledgeFileDTOList = new ArrayList<>();
        for (int i = 0; i < knowledgeDocumentList.size(); i++) {
            CandidateKnowledgeFileDTO candidateKnowledgeFileDTO = DtoConverter.toCandidateKnowledgeFileDTO(
                    knowledgeDocumentList.get(i),
                    ocrMissionDTOList.get(i),
                    documentSplittingMissionDTOList.get(i),
                    embeddingMissionDTOList.get(i)
            );
            candidateKnowledgeFileDTOList.add(candidateKnowledgeFileDTO);
        }

        // 10.封装成LazyPageDTO<CandidateKnowledgeFileDTO>并返回
        LazyPageDTO<CandidateKnowledgeFileDTO> candidateKnowledgeFileDTOLazyPageDTO = new LazyPageDTO<CandidateKnowledgeFileDTO>(false, candidateKnowledgeFileDTOList);

        return candidateKnowledgeFileDTOLazyPageDTO;
    }

    /**
     * 监听EmbeddingMissionSuccessEvent事件，将成功向量化后的文档添加到知识库。
     *
     * @param event 向量化任务成功事件
     */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void addAKnowledgeDocumentFromSuccessfulEmbeddingMission(EmbeddingMissionSuccessEvent event) {
        // 获取向量化任务
        EmbeddingMission embeddingMission = event.getEmbeddingMission();

        // 获取当前向量化任务的的状态
        MissionStatus status = embeddingMission.getStatus();

        // 判断当前状态是否成功
        if (status != MissionStatus.SUCCESS) {
            // 不成功，就报错
            throw new IllegalStateException("失败的EmbeddingMission无法成为一个KnowledgeDocument");
        }

        // 成功就保存产生的向量化文件
        KnowledgeDocument knowledgeDocument = KnowledgeDocument.Companion.create(
                UUID.randomUUID().toString(),
                sourceDocumentRepository.findSourceDocumentById(embeddingMission.getSourceDocumentId()).get().getName(),
                embeddingMission.getId(),
                KnowledgeDocumentType.STRUCTURE_SPLITTER
        );
        knowledgeDocumentRepository.save(knowledgeDocument);
    }
}
