package top.emilejones.hhu.pipeline.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.emilejones.hhu.pipeline.entity.StructureExtractionMissionPo;

import java.util.List;

/**
 * 结构化抽取任务数据库访问层
 * @author Yeyezhi
 */
@Mapper
public interface StructureExtractionMissionMapper {

    /**
     * 插入或更新结构化抽取任务
     * @param structureExtractionMissionPo 任务持久化对象
     */
    void upsertStructureExtractionMission(@Param("structureExtractionMission") StructureExtractionMissionPo structureExtractionMissionPo);

    /**
     * 批量插入或更新结构化抽取任务
     * @param structureExtractionMissionPoList 任务持久化对象列表
     */
    void upsertStructureExtractionMissionBatch(@Param("structureExtractionMissionList") List<StructureExtractionMissionPo> structureExtractionMissionPoList);

    /**
     * 根据源文档ID查询任务列表
     * @param sourceDocumentId 源文档ID
     * @return 任务持久化对象列表
     */
    List<StructureExtractionMissionPo> findBySourceDocumentId(@Param("sourceDocumentId") String sourceDocumentId);

    /**
     * 根据任务ID查询任务
     * @param structureExtractionMissionId 任务ID
     * @return 任务持久化对象
     */
    StructureExtractionMissionPo findById(@Param("structureExtractionMissionId") String structureExtractionMissionId);

    /**
     * 删除任务
     * @param structureExtractionMissionId 任务ID
     */
    void delete(@Param("structureExtractionMissionId") String structureExtractionMissionId);
}
