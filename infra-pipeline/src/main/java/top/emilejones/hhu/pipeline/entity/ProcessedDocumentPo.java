package top.emilejones.hhu.pipeline.entity;

import lombok.Data;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * OCR / 预处理后生成的文档持久化对象（PO）。
 *
 * 表示由 OCR 任务生成的结构化文档产物，例如：
 * - Markdown 文档
 * - 图片文件（OCR 后的中间产物）
 *
 * 该对象只描述“产物是什么、存在哪里”，
 * 不包含任何任务执行或业务行为。
 *
 * @author Yeyezhi
 */

@Data
public class ProcessedDocumentPo {
    /** 处理后文档唯一标识（UUID） */
    private String processedDocumentId;

    /** 源文档 ID */
    private String sourceDocumentId;

    /** 文档名称（如 xxx.md / page_1.png） */
    private String fileName;

    /** 文件存储路径 */
    private String filePath;

    /** 文档类型（0=MARKDOWN，1=IMAGE） */
    private ProcessedDocumentType type;

    /** 创建时间 */
    private Instant createTime;
}
