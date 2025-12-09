package top.emilejones.hhu.knowledge.pojo.dto;

import lombok.Data;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;

import java.time.Instant;

/**
 * 知识库信息传输对象
 * @author EmileNathon
 */
@Data
public class KnowledgeCatalogDto {
    /**
     * 主键，自增
     */
    private int id;

    /**
     * 知识库的唯一标识，UUID生成
     */
    private String kbId;

    /**
     * 知识库的名称
     */
    private String kbName;

    /**
     * milvus中collection的名称
     */
    private String colName;

    /**
     * 知识库的类型；0：字符切割， 1：文本结构切割
     */
    private KnowledgeCatalogType type;

    /**
     * 删除的标记，0：删除；1：存在
     */
    private int isDelete;

    /**
     * 创建记录的时间
     */
    private Instant createTime;

    /**
     * 知识库权限
     */
    private String permission;
}
