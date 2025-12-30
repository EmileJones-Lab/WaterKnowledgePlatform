package top.emilejones.hhu.document.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.emilejones.hhu.document.entity.SourceDocumentPO;

/**
 *
 * @author Yeyezhi
 */
@Mapper
public interface SourceDocumentMapper {

    SourceDocumentPO findById(@Param("file_id") String id);
}