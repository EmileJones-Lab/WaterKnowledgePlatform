package top.emilejones.hhu.pipeline.services;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult;
import top.emilejones.hhu.pipeline.entity.OcrMissionPo;
import top.emilejones.hhu.pipeline.mapper.OcrMissionMapper;
import top.emilejones.hhu.pipeline.utils.PoToDomainUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * OCR任务服务实现类
 * @author Yeyezhi
 */
@Service
public class OcrMissionService{

    private final OcrMissionMapper ocrMissionMapper;

    public OcrMissionService(OcrMissionMapper ocrMissionMapper) {
        this.ocrMissionMapper = ocrMissionMapper;
    }


    /**
     * 保存单个OCR任务；若已有同标识任务，将覆盖旧记录。
     *
     * 约定：具备 upsert 语义，重复写入需覆盖旧任务。
     *
     * @param ocrMission 待保存的OCR任务领域对象，不能为null
     */
    public void save(@NotNull OcrMission ocrMission) {
        OcrMissionPo po = convertToPo(ocrMission);
        ocrMissionMapper.upsertOcrMission(po);
    }

    /**
     * 批量保存OCR任务；遇到重复标识执行覆盖（upsert）。
     *
     * 约定：具备 upsert 语义；应保证部分失败可定位，必要时支持局部回滚或幂等重试。
     *
     * @param ocrMissionList 待保存的OCR任务集合，不能为null
     */
    public void saveBatch(@NotNull List<OcrMission> ocrMissionList) {
        if (ocrMissionList.isEmpty()) {
            return;
        }

        List<OcrMissionPo> poList = ocrMissionList.stream()
                .map(this::convertToPo)
                .toList();

        ocrMissionMapper.upsertOcrMissionBatch(poList);
    }

    /**
     * 查询最近启动的OCR任务对应的源文件。（5种状态的任务都算启动）
     *
     * 约定：需支持 limit/offset 分页，并按创建时间倒序返回。
     *
     * @param limit 限制返回数量，必须大于等于0
     * @param offset 偏移量，用于分页，必须大于等于0
     * @return 源文件标识列表，按创建时间倒序，若无记录则返回空列表
     */
    @NotNull
    public List<String> findStartOcrMissionSourceDocumentIdByCreateTimeDesc(int limit, int offset) {
        if (limit < 0 || offset < 0) {
            throw new IllegalArgumentException("Limit and offset must be non-negative");
        }
        return ocrMissionMapper.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit, offset);
    }

    /**
     * 根据源文档查询任务列表。
     *
     * 约定：未命中时返回空列表；任务按创建时间倒序返回。
     *
     * @param sourceDocumentId 源文档标识，不能为null或空
     * @return 该文档关联的OCR任务列表，按创建时间倒序，无记录时返回空列表
     */
    public List<OcrMission> findBySourceDocumentId(String sourceDocumentId) {
        if (sourceDocumentId == null || sourceDocumentId.isBlank()) {
            throw new IllegalArgumentException("Source document ID cannot be blank");
        }

        List<OcrMissionPo> poList = ocrMissionMapper.findBySourceDocumentId(sourceDocumentId);
        return poList.stream()
                .map(PoToDomainUtil::toOcrMissionDomain)
                .toList();
    }

    /**
     * 批量查询任务列表。
     *
     * 约定：结果顺序需与入参保持一致；缺失项返回空列表。
     *
     * @param sourceDocumentIdList 源文档标识集合，不能为null
     * @return 与入参顺序一致的任务列表集合，每个源文档对应的任务列表
     */
    @NotNull
    public List<List<OcrMission>> findBatchBySourceDocumentId(@NotNull List<String> sourceDocumentIdList) {
        if (sourceDocumentIdList.isEmpty()) {
            return List.of();
        }

        return sourceDocumentIdList.stream()
                .map(sourceDocumentId -> {
                    List<OcrMissionPo> poList = ocrMissionMapper.findBySourceDocumentId(sourceDocumentId);
                    return poList.stream()
                            .map(PoToDomainUtil::toOcrMissionDomain)
                            .toList();
                })
                .toList();
    }


    /**
     * 删除OCR任务。
     *
     * 约定：删除操作幂等，重复删除不抛出异常；未命中时静默返回。
     *
     * @param ocrMissionId 任务标识，不能为null或空
     */
    public void delete(String ocrMissionId) {
        if (ocrMissionId == null || ocrMissionId.isBlank()) {
            throw new IllegalArgumentException("OCR mission ID cannot be blank");
        }

        ocrMissionMapper.delete(ocrMissionId);
    }


    /**
     * 根据ID查找OCR任务。
     *
     * @param ocrMissionId 任务标识，不能为null或空
     * @return OCR任务对象，不存在时返回null
     */
    @Nullable
    public OcrMission findById(@NotNull String ocrMissionId) {
        if (ocrMissionId.isBlank()) {
            throw new IllegalArgumentException("OCR mission ID cannot be blank");
        }

        OcrMissionPo po = ocrMissionMapper.findById(ocrMissionId);
        return po != null ? PoToDomainUtil.toOcrMissionDomain(po) : null;
    }
    /**
     * 封装领域对象OcrMission到持久化对象OcrMissionPo中
     */
    private OcrMissionPo convertToPo(OcrMission ocrMission) {
        OcrMissionPo po = new OcrMissionPo();
        po.setOcrMissionId(ocrMission.getId());
        po.setSourceDocumentId(ocrMission.getSourceDocumentId());
        po.setStatusType(ocrMission.getStatus());
        po.setCreateTime(ocrMission.getCreateTime());
        po.setStartTime(ocrMission.getStartTime());
        po.setEndTime(ocrMission.getEndTime());

        // 处理结果信息：保留源文件ID，按结果填充 processed/error
        OcrMissionResult r = ocrMission.getResult();
        if (r instanceof OcrMissionResult.Success s) {
            po.setProcessedDocumentId(s.getMarkdownDocumentId());
            po.setErrorMessage(null);
        } else if (r instanceof OcrMissionResult.Failure f) {
            po.setProcessedDocumentId(null);
            po.setErrorMessage(f.getErrorMessage());
        } else {
            po.setProcessedDocumentId(null);
            po.setErrorMessage(null);
        }
        return po;
    }

}
