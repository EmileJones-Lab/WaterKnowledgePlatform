package top.emilejones.hhu.pipeline.services;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMissionResult;
import top.emilejones.hhu.pipeline.constant.DeleteConstant;
import top.emilejones.hhu.pipeline.entity.EmbeddingMissionPo;
import top.emilejones.hhu.pipeline.mapper.EmbeddingMissionMapper;
import top.emilejones.hhu.pipeline.utils.PoToDomainUtil;

import java.util.List;

/**
 * 向量化任务服务实现类
 *
 * @author Yeyezhi
 */
@Service
public class EmbeddingMissionService {

    private final EmbeddingMissionMapper embeddingMissionMapper;

    public EmbeddingMissionService(EmbeddingMissionMapper embeddingMissionMapper) {
        this.embeddingMissionMapper = embeddingMissionMapper;
    }

    /**
     * 保存单个向量化任务；若已有同标识任务，将覆盖旧记录。
     * 约定：具备 upsert 语义，重复写入需覆盖旧任务。
     *
     * @param embeddingMission 待保存的向量化任务领域对象，不能为null
     */
    public void save(@NotNull EmbeddingMission embeddingMission) {
        EmbeddingMissionPo po = convertToPo(embeddingMission);
        embeddingMissionMapper.upsertEmbeddingMission(po);
    }

    /**
     * 批量保存向量化任务；遇到重复标识执行覆盖（upsert）。
     * <p>
     * 约定：具备 upsert 语义；应保证部分失败可定位，必要时支持局部回滚或幂等重试。
     *
     * @param embeddingMissionList 待保存的向量化任务集合，不能为null
     */
    public void saveBatch(@NotNull List<EmbeddingMission> embeddingMissionList) {
        if (embeddingMissionList.isEmpty()) {
            return;
        }

        List<EmbeddingMissionPo> poList = embeddingMissionList.stream()
                .map(this::convertToPo)
                .toList();

        embeddingMissionMapper.upsertEmbeddingMissionBatch(poList);
    }

    /**
     * 根据源文档查询任务列表。
     * <p>
     * 约定：未命中时返回空列表；任务按创建时间倒序返回。
     *
     * @param sourceDocumentId 源文档标识，不能为null或空
     * @return 该文档关联的向量化任务列表，按创建时间倒序，无记录时返回空列表
     */
    public List<EmbeddingMission> findBySourceDocumentId(String sourceDocumentId) {
        if (sourceDocumentId == null || sourceDocumentId.isBlank()) {
            throw new IllegalArgumentException("Source document ID cannot be blank");
        }

        List<EmbeddingMissionPo> poList = embeddingMissionMapper.findBySourceDocumentId(sourceDocumentId);
        return poList.stream()
                .map(PoToDomainUtil::toEmbeddingDomain)
                .toList();
    }

    /**
     * 批量查询任务列表。
     * <p>
     * 约定：结果顺序需与入参保持一致；缺失项返回空列表。
     *
     * @param sourceDocumentIdList 源文档标识集合，不能为null
     * @return 与入参顺序一致的任务列表集合，每个源文档对应的任务列表
     */
    @NotNull
    public List<List<EmbeddingMission>> findBatchBySourceDocumentId(@NotNull List<String> sourceDocumentIdList) {
        if (sourceDocumentIdList.isEmpty()) {
            return List.of();
        }

        return sourceDocumentIdList.stream()
                .map(sourceDocumentId -> {
                    List<EmbeddingMissionPo> poList = embeddingMissionMapper.findBySourceDocumentId(sourceDocumentId);
                    return poList.stream()
                            .map(PoToDomainUtil::toEmbeddingDomain)
                            .toList();
                })
                .toList();
    }

    /**
     * 软删除指定的向量化文件。
     * 该操作通过更新文档的isDelete字段来标记删除，而非物理删除。
     *
     * @param embeddingMissionId 待删除向量化文件的ID。
     */
    public void delete(String embeddingMissionId) {
        if (embeddingMissionId == null || embeddingMissionId.isBlank()) {
            throw new IllegalArgumentException("Embedding mission ID cannot be blank");
        }

        EmbeddingMissionPo embeddingMissionPo = new EmbeddingMissionPo();
        // 对需要删除的信息封装成EmbeddingMissionPo对象

        embeddingMissionPo.setEmbeddingMissionId(embeddingMissionId);
        embeddingMissionPo.setIsDelete(DeleteConstant.DELETE);
        //删除对应的向量化文件，因为这里是软删除所以调用的是update方法
        embeddingMissionMapper.softDelete(embeddingMissionPo);
    }

    /**
     * 根据ID查找向量化任务。
     *
     * @param embeddingMissionId 任务标识，不能为null或空
     * @return 向量化任务对象，不存在时返回null
     */
    @Nullable
    public EmbeddingMission findById(String embeddingMissionId) {
        if (embeddingMissionId == null || embeddingMissionId.isBlank()) {
            throw new IllegalArgumentException("Embedding mission ID cannot be blank");
        }

        EmbeddingMissionPo po = embeddingMissionMapper.findById(embeddingMissionId);
        return po != null ? PoToDomainUtil.toEmbeddingDomain(po) : null;
    }

    /**
     * 封装领域对象EmbeddingMission到持久化对象EmbeddingMissionPo中
     *
     * @param embeddingMission 向量化任务领域对象
     * @return 向量化任务持久化对象
     */
    private EmbeddingMissionPo convertToPo(EmbeddingMission embeddingMission) {
        EmbeddingMissionPo po = new EmbeddingMissionPo();
        po.setEmbeddingMissionId(embeddingMission.getId());
        po.setSourceDocumentId(embeddingMission.getSourceDocumentId());
        po.setFileNodeId(embeddingMission.getFileNodeId());
        po.setStatusType(embeddingMission.getStatus());
        po.setCreateTime(embeddingMission.getCreateTime());
        po.setStartTime(embeddingMission.getStartTime());
        po.setEndTime(embeddingMission.getEndTime());
        // 设置删除标记为存在状态
        po.setIsDelete(DeleteConstant.EXIST);

        // 处理结果信息：保留源文件ID，按结果填充 FileNodeId/errorMessage
        EmbeddingMissionResult r = embeddingMission.getResult();
        if (r instanceof EmbeddingMissionResult.Success s) {
            po.setFileNodeId(s.getFileNodeId());
            po.setErrorMessage(null);
        } else if (r instanceof EmbeddingMissionResult.Failure f) {
            po.setFileNodeId(embeddingMission.getFileNodeId()); // 保留原有的fileNodeId
            po.setErrorMessage(f.getErrorMessage());
        } else {
            // 初始状态或运行中状态，保持原有值
            po.setErrorMessage(null);
        }
        return po;
    }
}
