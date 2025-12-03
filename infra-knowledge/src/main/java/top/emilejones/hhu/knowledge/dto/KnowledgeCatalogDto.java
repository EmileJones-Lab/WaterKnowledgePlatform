package top.emilejones.hhu.knowledge.entity;

import lombok.Data;

import java.time.Instant;

@Data
public class KnowledgeCatalog {
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
     * 知识库的类型；0：字符切割， 1：文本结构切割
     */
    private int type;

    /**
     * 创建记录的时间
     */
    private Instant createTime;

    /**
     * 文件权限
     */
    private String permission;
}
