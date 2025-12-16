package top.emilejones.hhu.pipeline.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.emilejones.hhu.pipeline.entity.OcrMissionPo;

import java.util.List;

/**
 * OCR任务数据库访问层
 * @author Yeyezhi
 */
@Mapper
public interface OcrMissionMapper {

    /**
     * 插入或更新OCR任务
     * @param ocrMissionPo OCR任务持久化对象
     */
    void upsertOcrMission(@Param("ocrMission") OcrMissionPo ocrMissionPo);

    /**
     * 批量插入或更新OCR任务
     * @param ocrMissionPoList OCR任务持久化对象列表
     */
    void upsertOcrMissionBatch(@Param("ocrMissionList") List<OcrMissionPo> ocrMissionPoList);

    /**
     * 查询最近启动的OCR任务对应的源文件ID
     * @param limit 限制返回数量
     * @param offset 偏移量
     * @return 源文件ID列表
     */
    List<String> findStartOcrMissionSourceDocumentIdByCreateTimeDesc(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 根据源文档ID查询OCR任务列表
     * @param sourceDocumentId 源文档ID
     * @return OCR任务持久化对象列表
     */
    List<OcrMissionPo> findBySourceDocumentId(@Param("sourceDocumentId") String sourceDocumentId);

    /**
     * 根据OCR任务ID查询任务
     * @param ocrMissionId OCR任务ID
     * @return OCR任务持久化对象
     */
    OcrMissionPo findById(@Param("ocrMissionId") String ocrMissionId);

    /**
     * 删除OCR任务
     * @param ocrMissionId OCR任务ID
     */
    void delete(@Param("ocrMissionId") String ocrMissionId);
}
