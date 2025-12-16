package top.emilejones.hhu.pipeline.entity;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 结构化抽取任务持久化对象（PO）。
 *
 * 表示一次针对 ProcessedDocument 执行的结构抽取任务，
 * 负责将文档内容解析为结构化的图数据（如 Neo4j 中的 FileNode、TextNode）。
 *
 * 该对象用于记录结构化抽取任务的执行状态、
 * 失败原因以及最终生成的 FileNode 标识，
 * 不包含具体的抽取算法实现。
 *
 * @author Yeyezhi
 */

@Data
public class StructureExtractionMissionPo {
    /** 结构化抽取任务 ID（UUID） */
    private String structureExtractionMissionId;

    /** 源文档 ID */
    private String sourceDocumentId;

    /** 使用的 ProcessedDocument ID */
    private String processedDocumentId;

    /** 任务状态（0=待执行，1=执行中，2=成功，3=失败） */
    private Integer statusType;

    /** 生成的 Neo4j FileNode elementId */
    private String FileNodeElementId;

    /** 失败原因 */
    private String errorMessage;

    /** 创建时间 */
    private Instant createTime;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;
}
