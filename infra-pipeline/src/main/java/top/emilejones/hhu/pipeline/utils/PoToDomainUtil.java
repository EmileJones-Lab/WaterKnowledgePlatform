package top.emilejones.hhu.pipeline.utils;

import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMissionResult;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult;
import top.emilejones.hhu.pipeline.entity.EmbeddingMissionPo;
import top.emilejones.hhu.pipeline.entity.OcrMissionPo;
import top.emilejones.hhu.pipeline.entity.ProcessedDocumentPo;
import top.emilejones.hhu.pipeline.entity.StructureExtractionMissionPo;

/**
 * PoToDomainUtil 是一个工具类，负责将数据传输对象（PO）转换为领域模型对象。
 * 它提供了将各种PO封装成对应的领域对象的方法。
 *
 * @author Yeyezhi
 */
public class PoToDomainUtil {

    private PoToDomainUtil() {}

    /**
     * 将持久化对象OcrMissionPo转换为领域对象OcrMission
     *
     * @param ocrMissionPo 待转换的OCR任务持久化对象
     * @return 封装后的 OcrMission 领域模型对象。
     */
    public static OcrMission toOcrMissionDomain(OcrMissionPo ocrMissionPo) {
        MissionStatus status = ocrMissionPo.getStatusType();

        OcrMissionResult result = null;
        if (status == MissionStatus.SUCCESS) {
            String processedDocumentId = ocrMissionPo.getProcessedDocumentId();
            if (processedDocumentId != null && !processedDocumentId.isBlank()) {
                // 不改 domain 的前提下：把 processedDocumentId 塞到 result的markdownDocumentId 里
                result = new OcrMissionResult.Success(processedDocumentId);
            }
        } else if (status == MissionStatus.ERROR) {
            String msg = ocrMissionPo.getErrorMessage();
            if (msg != null && !msg.isBlank()) {
                result = new OcrMissionResult.Failure(msg);
            }
        }
        return new OcrMission(
                ocrMissionPo.getOcrMissionId(),
                ocrMissionPo.getSourceDocumentId(),
                status,
                result,
                ocrMissionPo.getCreateTime(),
                ocrMissionPo.getStartTime(),
                ocrMissionPo.getEndTime()
        );
    }

    /**
     * 将持久化对象ProcessedDocumentPo转换为领域对象ProcessedDocument
     *
     * @param processedDocumentPo 待转换的Ocr后的持久化对象
     * @return 封装后的 ProcessedDocument 领域模型对象。
     */
    public static ProcessedDocument toProcessedDocumentDomain(ProcessedDocumentPo processedDocumentPo) {
        return new ProcessedDocument(
                processedDocumentPo.getProcessedDocumentId(),
                processedDocumentPo.getSourceDocumentId(),
                processedDocumentPo.getFilePath(),
                processedDocumentPo.getCreateTime(),
                processedDocumentPo.getType()
        );
    }


    /**
     * 将持久化对象StructureExtractionMissionPo转换为领域对象StructureExtractionMission
     *
     * @param structureExtractionMissionPo 待转换的结构提取任务持久化对象
     * @return 封装后的 StructureExtractionMission 领域模型对象。
     */
    public static StructureExtractionMission toStructureExtractionDomain(StructureExtractionMissionPo structureExtractionMissionPo) {

        MissionStatus status = structureExtractionMissionPo.getStatusType();

        StructureExtractionMissionResult result = null;
        if (status == MissionStatus.SUCCESS) {
            String fileNodeId = structureExtractionMissionPo.getFileNodeId();
            if (fileNodeId != null && !fileNodeId.isBlank()) {
                result = new StructureExtractionMissionResult.Success(fileNodeId);
            }
        } else if (status == MissionStatus.ERROR) {
            String msg = structureExtractionMissionPo.getErrorMessage();
            if (msg != null && !msg.isBlank()) {
                result = new StructureExtractionMissionResult.Failure(msg);
            }
        }

        return new StructureExtractionMission(
                structureExtractionMissionPo.getStructureExtractionMissionId(),
                structureExtractionMissionPo.getSourceDocumentId(),
                structureExtractionMissionPo.getProcessedDocumentId(),
                status,
                result,
                structureExtractionMissionPo.getCreateTime(),
                structureExtractionMissionPo.getStartTime(),
                structureExtractionMissionPo.getEndTime()
        );
    }


    /**
     * 将持久化对象EmbeddingMissionPo转换为领域对象EmbeddingMission
     *
     * @param embeddingMissionPo 待转换的向量化任务持久化对象
     * @return 封装后的 EmbeddingMission 领域模型对象。
     */
    public static EmbeddingMission toEmbeddingDomain(EmbeddingMissionPo embeddingMissionPo) {

        MissionStatus status = embeddingMissionPo.getStatusType();

        EmbeddingMissionResult result = null;
        if (status == MissionStatus.SUCCESS) {
            String fileNodeId = embeddingMissionPo.getFileNodeId();
            if (fileNodeId != null && !fileNodeId.isBlank()) {
                result = new EmbeddingMissionResult.Success(fileNodeId);
            }
        } else if (status == MissionStatus.ERROR) {
            String msg = embeddingMissionPo.getErrorMessage();
            if (msg != null && !msg.isBlank()) {
                result = new EmbeddingMissionResult.Failure(msg);
            }
        }

        return new EmbeddingMission(
                embeddingMissionPo.getEmbeddingMissionId(),
                embeddingMissionPo.getSourceDocumentId(),
                embeddingMissionPo.getFileNodeId(),
                status,
                result,
                embeddingMissionPo.getCreateTime(),
                embeddingMissionPo.getStartTime(),
                embeddingMissionPo.getEndTime()
        );
    }

}
