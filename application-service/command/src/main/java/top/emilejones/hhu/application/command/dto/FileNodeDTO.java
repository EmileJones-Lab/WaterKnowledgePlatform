package top.emilejones.hhu.application.command.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 文件节点数据传输对象。
 * 用于在应用层和表现层之间传输文件节点信息。
 */
@Data
@Builder
public class FileNodeDTO {
    /** 文件节点唯一标识 */
    private String id;
    /** 源文件ID */
    private String sourceDocumentId;
    /** 是否已向量化 */
    private boolean isEmbedded;
    /** 文件摘要 */
    private String fileAbstract;
    /** 文件名称 */
    private String fileName;
}
