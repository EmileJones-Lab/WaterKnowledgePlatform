package top.emilejones.hhu.pipeline.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.emilejones.hhu.pipeline.entity.OcrMissionPo;
import top.emilejones.hhu.pipeline.entity.ProcessedDocumentPo;

import java.util.List;

/**
 * Ocr处理后文档数据库访问层
 * @author Yeyezhi
 */
@Mapper
public interface ProcessedDocumentMapper {

    /**
     * 插入或更新处理后文档
     * @param processedDocument 处理后文档持久化对象
     */
    void upsertProcessedDocument(@Param("processedDocument") ProcessedDocumentPo processedDocument);

    /**
     * 根据文档ID查询处理后文档
     * @param processedDocumentId 处理后文档ID
     * @return 处理后文档持久化对象
     */
    ProcessedDocumentPo findById(@Param("processedDocumentId") String processedDocumentId);

    /**
     * 根据源文档ID查询处理后文档列表
     * @param sourceDocumentId 源文档ID
     * @return 处理后文档持久化对象列表
     */
    List<ProcessedDocumentPo> findBySourceDocumentId(@Param("sourceDocumentId") String sourceDocumentId);

   /**
     * 更新Ocr处理后的文档
     * @param processedDocumentPo
     */
    void update(ProcessedDocumentPo processedDocumentPo);

    /**
     * 仅供测试使用：物理清空表数据
     */
    @Update("DELETE FROM processed_document")
    void truncateTable();
}
