package top.emilejones.hhu.pipeline.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.emilejones.hhu.pipeline.entity.EmbeddingMissionPo;

import java.util.List;

/**
 * 向量化任务数据库访问层
 * @author Yeyezhi
 */
@Mapper
public interface EmbeddingMissionMapper {

    /**
     * 插入或更新向量化任务
     * @param embeddingMission 向量化任务持久化对象
     */
    void upsertEmbeddingMission(@Param("embeddingMission") EmbeddingMissionPo embeddingMission);

    /**
     * 批量插入或更新向量化任务
     * @param embeddingMissionList 向量化任务持久化对象列表
     */
    void upsertEmbeddingMissionBatch(@Param("embeddingMissionList") List<EmbeddingMissionPo> embeddingMissionList);

    /**
     * 根据源文档ID查询向量化任务列表
     * @param sourceDocumentId 源文档ID
     * @return 向量化任务持久化对象列表
     */
    List<EmbeddingMissionPo> findBySourceDocumentId(@Param("sourceDocumentId") String sourceDocumentId);

    /**
     * 根据向量化任务ID查询任务
     * @param embeddingMissionId 向量化任务ID
     * @return 向量化任务持久化对象
     */
    EmbeddingMissionPo findById(@Param("embeddingMissionId") String embeddingMissionId);

    /**
     * 删除向量化任务
     * @param embeddingMissionId 向量化任务ID
     */
    void delete(@Param("embeddingMissionId") String embeddingMissionId);
}
