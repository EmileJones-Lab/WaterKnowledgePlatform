package top.emilejones.hhu.application.command.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 文本节点数据传输对象。
 * 用于在应用层和展示层之间传输文本节点的信息。
 */
@Data
@Builder
public class TextNodeDTO {
    /** 唯一标识符 */
    private String id;
    /** 原始文件节点ID */
    private String fileNodeId;
    /** 文本内容 */
    private String text;
    /** 序列号 */
    private int seq;
    /** 层级深度 */
    private int level;
    /** 节点类型（如：普通文本、表格等） */
    private String type;
    /** 所属文件名 */
    private String fileName;
    /** 摘要信息 */
    private String summary;
}
