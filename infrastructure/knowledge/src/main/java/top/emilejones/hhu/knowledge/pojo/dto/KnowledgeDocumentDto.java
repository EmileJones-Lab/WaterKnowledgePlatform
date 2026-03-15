package top.emilejones.hhu.knowledge.pojo.dto;

import lombok.Data;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;

import java.time.Instant;

/**
 * 向量化文件的数据传输对象
 * @author EmileNathon
 */
@Data
public class KnowledgeDocumentDto {
    /**
     * 主键，自增
     */
    private int id;

    /**
     * 向量化文件id
     */
    private String documentId;

    /**
     * 向量化文件名称
     */
    private String documentName;

    /**
     * embed任务id
     */
    private String embedId;

    /**
     * 向量化文件类型
     */
    private KnowledgeDocumentType type;

    /**
     * 文件删除的标记
     * 0：记录删除， 1：记录未删除， 默认为1
     */
    private int isDelete;

    /**
     * 创建的时间
     */
    private Instant createTime;

}
