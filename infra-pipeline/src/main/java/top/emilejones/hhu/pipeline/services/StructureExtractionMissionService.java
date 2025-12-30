package top.emilejones.hhu.pipeline.services;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult;
import top.emilejones.hhu.pipeline.constant.DeleteConstant;
import top.emilejones.hhu.pipeline.entity.StructureExtractionMissionPo;
import top.emilejones.hhu.pipeline.mapper.StructureExtractionMissionMapper;
import top.emilejones.hhu.pipeline.utils.PoToDomainUtil;

import java.util.List;

/**
 * 结构化抽取任务服务实现类，负责结构化任务的持久化与查询协调。
 * @author Yeyezhi
 */
@Service
public class StructureExtractionMissionService {

    private final StructureExtractionMissionMapper structureExtractionMissionMapper;

    public StructureExtractionMissionService(StructureExtractionMissionMapper structureExtractionMissionMapper) {
        this.structureExtractionMissionMapper = structureExtractionMissionMapper;
    }


    /**
     * 保存单个结构化抽取任务，具备 upsert 语义。
     * @param structureExtractionMission 任务领域对象
     */
    public void save(@NotNull StructureExtractionMission structureExtractionMission) {
        StructureExtractionMissionPo po = convertToPo(structureExtractionMission);
        structureExtractionMissionMapper.upsertStructureExtractionMission(po);
    }


    /**
     * 批量保存结构化抽取任务；重复主键覆盖。
     * @param structureExtractionMissionList 待保存任务列表
     */
    public void saveBatch(@NotNull List<StructureExtractionMission> structureExtractionMissionList) {
        if (structureExtractionMissionList.isEmpty()) {
            return;
        }

        List<StructureExtractionMissionPo> poList = structureExtractionMissionList.stream()
                .map(this::convertToPo)
                .toList();
        structureExtractionMissionMapper.upsertStructureExtractionMissionBatch(poList);
    }

    /**
     * 根据源文档ID查询任务，按创建时间倒序返回。
     * @param sourceDocumentId 源文档标识
     * @return 任务列表，未命中返回空列表
     */
    @NotNull
    public List<StructureExtractionMission> findBySourceDocumentId(String sourceDocumentId) {
        if (sourceDocumentId == null ||sourceDocumentId.isBlank()) {
            throw new IllegalArgumentException("Source document ID cannot be blank");
        }

        List<StructureExtractionMissionPo> poList = structureExtractionMissionMapper.findBySourceDocumentId(sourceDocumentId);
        return poList.stream()
                .map(PoToDomainUtil::toStructureExtractionDomain)
                .toList();
    }


    /**
     * 批量按源文档ID查询任务，结果顺序与入参一致。
     * @param sourceDocumentIdList 源文档ID列表
     * @return 每个源文档对应的任务列表集合
     */
    @NotNull
    public List<List<StructureExtractionMission>> findBySourceDocumentIdList(@NotNull List<String> sourceDocumentIdList) {
        if (sourceDocumentIdList.isEmpty()) {
            return List.of();
        }

        return sourceDocumentIdList.stream()
                .map(this::findBySourceDocumentId)
                .toList();
    }


    /**
     * 软删除指定的向量化文件。
     * 该操作通过更新文档的isDelete字段来标记删除，而非物理删除。
     *
     * @param structureExtractionMissionId 待删除向量化文件的ID。
     */
    public void delete(String structureExtractionMissionId) {
        if (structureExtractionMissionId == null || structureExtractionMissionId.isBlank()) {
            throw new IllegalArgumentException("Embedding mission ID cannot be blank");
        }
        StructureExtractionMissionPo structureExtractionMissionPo = new StructureExtractionMissionPo();
        // 对需要删除的信息封装成StructureExtractionMissionPo对象

        structureExtractionMissionPo.setStructureExtractionMissionId(structureExtractionMissionId);
        structureExtractionMissionPo.setIsDelete(DeleteConstant.DELETE);

        //删除对应的向量化文件，因为这里是软删除所以调用的是update方法
        structureExtractionMissionMapper.softDelete(structureExtractionMissionPo);
    }

    /**
     * 根据ID查找结构化抽取任务。
     * @param structureExtractionMissionId 任务标识
     * @return 任务对象，不存在返回 null
     */
    @Nullable
    public StructureExtractionMission findById(String structureExtractionMissionId) {
        if (structureExtractionMissionId == null || structureExtractionMissionId.isBlank()) {
            throw new IllegalArgumentException("Structure extraction mission ID cannot be blank");
        }

        StructureExtractionMissionPo po = structureExtractionMissionMapper.findById(structureExtractionMissionId);
        return po != null ? PoToDomainUtil.toStructureExtractionDomain(po) : null;
    }

    private StructureExtractionMissionPo convertToPo(StructureExtractionMission structureExtractionMission) {
        StructureExtractionMissionPo po = new StructureExtractionMissionPo();
        po.setStructureExtractionMissionId(structureExtractionMission.getId());
        po.setSourceDocumentId(structureExtractionMission.getSourceDocumentId());
        po.setProcessedDocumentId(structureExtractionMission.getProcessedDocumentId());
        po.setStatusType(structureExtractionMission.getStatus());
        po.setCreateTime(structureExtractionMission.getCreateTime());
        po.setStartTime(structureExtractionMission.getStartTime());
        po.setEndTime(structureExtractionMission.getEndTime());
        // 设置删除标记为存在状态
        po.setIsDelete(DeleteConstant.EXIST);

        StructureExtractionMissionResult result = structureExtractionMission.getResult();
        if (result instanceof StructureExtractionMissionResult.Success success) {
            po.setFileNodeId(success.getFileNodeId());
            po.setErrorMessage(null);
        } else if (result instanceof StructureExtractionMissionResult.Failure failure) {
            po.setFileNodeId(null);
            po.setErrorMessage(failure.getErrorMessage());
        } else {
            po.setFileNodeId(null);
            po.setErrorMessage(null);
        }
        return po;
    }
}
