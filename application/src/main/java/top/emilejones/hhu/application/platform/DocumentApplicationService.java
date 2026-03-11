package top.emilejones.hhu.application.platform;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.application.platform.dto.LazyPageDTO;
import top.emilejones.hhu.application.platform.dto.mission.MissionsDTO;
import top.emilejones.hhu.application.platform.dto.retrieval.TextNodeDTO;
import top.emilejones.hhu.application.platform.utils.ListDtoConverter;
import top.emilejones.hhu.domain.document.SourceDocument;
import top.emilejones.hhu.domain.document.repository.SourceDocumentRepository;
import top.emilejones.hhu.domain.result.MissionStatus;
import top.emilejones.hhu.domain.result.TextNode;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository;
import top.emilejones.hhu.domain.pipeline.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocumentApplicationService {
    private final SourceDocumentRepository sourceDocumentRepository;
    private final NodeRepository nodeRepository;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final OcrMissionRepository ocrMissionRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;

    public DocumentApplicationService(SourceDocumentRepository sourceDocumentRepository, NodeRepository nodeRepository, StructureExtractionMissionRepository structureExtractionMissionRepository, OcrMissionRepository ocrMissionRepository, EmbeddingMissionRepository embeddingMissionRepository) {
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.nodeRepository = nodeRepository;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.ocrMissionRepository = ocrMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
    }

    /**
     * 获取一个文件的层次结构
     *
     * @param fileId 文件的唯一Id
     * @return 文件结构化数据
     */
    public List<TextNodeDTO> getFileStructureByFileId(String fileId) {
        Optional<StructureExtractionMission> successStructureExtractionMissionOptional = structureExtractionMissionRepository.findBySourceDocumentId(fileId)
                .stream()
                .filter(mission -> MissionStatus.SUCCESS.equals(mission.getStatus()))
                .findFirst();

        if (successStructureExtractionMissionOptional.isEmpty())
            throw new IllegalStateException("还没有提取出结构化文本");

        String fileNodeId = successStructureExtractionMissionOptional.get().getSuccessResult().getFileNodeId();
        List<TextNode> textNodeList = nodeRepository.findTextNodeListByFileNodeId(fileNodeId);
        return ListDtoConverter.toTextNodeDTOList(textNodeList);
    }

    /**
     * 分页的获取已经开启了任意任务的文件信息列表
     *
     * @param limit      每页多少个数据
     * @param pageNum    第几页（从0开始）
     * @param keyword    模糊匹配文件名
     * @param hasMission 是否只返回有任务开启的文件列表
     * @return 任务列表
     */
    public LazyPageDTO<MissionsDTO> getMissionsList(Integer limit, Integer pageNum, String keyword, Boolean hasMission) {
        if (!hasMission)
            throw new UnsupportedOperationException("目前只支持查询开启任务的文件信息列表");
        List<String> fileIdList = ocrMissionRepository.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit + 1, pageNum * limit, keyword);
        boolean hasNextPage = false;
        if (fileIdList.size() > limit) {
            hasNextPage = true;
            fileIdList.remove(limit.intValue());
        }
        List<SourceDocument> sourceDocuments = fileIdList.stream()
                .map(sourceDocumentRepository::findSourceDocumentById)
                .map(Optional::get)
                .toList();

        List<List<OcrMission>> ocrMissions = ocrMissionRepository.findBatchBySourceDocumentId(fileIdList);
        List<List<StructureExtractionMission>> splitterMissions = structureExtractionMissionRepository.findBySourceDocumentIdList(fileIdList);
        List<List<EmbeddingMission>> embeddingMissions = embeddingMissionRepository.findBatchBySourceDocumentId(fileIdList);

        List<MissionsDTO> missionsDTOS = ListDtoConverter.toMissionsDTOList(sourceDocuments, ocrMissions, splitterMissions, embeddingMissions);

        return new LazyPageDTO<>(hasNextPage, missionsDTOS);
    }
}
