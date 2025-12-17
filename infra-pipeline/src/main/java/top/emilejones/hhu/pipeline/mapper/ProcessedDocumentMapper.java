package top.emilejones.hhu.pipeline.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.emilejones.hhu.pipeline.entity.ProcessedDocumentPo;

import java.util.List;

/**
 * 处理后文档数据库访问层
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
     * 删除处理后文档
     * @param processedDocumentId 处理后文档ID
     */
    void delete(@Param("processedDocumentId") String processedDocumentId);
}
