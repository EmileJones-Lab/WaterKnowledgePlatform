package top.emilejones.hhu.knowledge.pojo.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CollectionDocumentDto {
    /**
     * 主键， 自增
     */
    private int id;

    /**
     * 知识库id
     */
    private String kbId;

    /**
     * 向量化文件id
     */
    private String documentId;

    /**
     * 删除标记
     * 0：删除，1：未删除；默认为1
     */
    private int isDelete;

    /**
     * 绑定的时间
     */
    private Instant createTime;
}
