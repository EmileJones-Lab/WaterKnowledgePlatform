package top.emilejones.hhu.knowledge.pojo.po;

import lombok.Data;

import java.time.Instant;

/**
 * 与collection_document数据库进行交互
 */
@Data
public class CollectionDocumentPo {
    /**
     * 主键，自增
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
     * 删除的标识
     * 0: 已经删除， 1：没有删除，默认是1
     */
    private int isDelete;

    /**
     * 创建时间
     */
    private Instant createTime;

}
