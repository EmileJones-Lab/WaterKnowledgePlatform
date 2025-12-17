package top.emilejones.hhu.pipeline.entity;

import lombok.Data;
import top.emilejones.hhu.domain.pipeline.MissionStatus;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 向量化任务持久化对象（PO）。
 *
 * 表示一次针对结构化结果（FileNode）的向量化处理任务，
 * 用于将图节点内容转换为向量并写入向量存储系统（如 Milvus）。
 *
 * 该对象负责记录向量化任务的执行状态、
 * 输入节点标识以及失败信息，
 * 本身不包含任何向量计算或存储逻辑。
 *
 * @author Yeyezhi
 */

@Data
public class EmbeddingMissionPo {
    /** 向量化任务 ID（UUID） */
    private String embeddingMissionId;

    /** 源文档 ID */
    private String sourceDocumentId;

    /** 输入的 Neo4j FileNode elementId */
    private String FileNodeId;

    /** 任务状态（0=创建成功，1=等待中，2=运行中，3=任务失败，4=任务成功） */
    private MissionStatus statusType;

    /** 失败原因 */
    private String errorMessage;

    /** 创建时间 */
    private Instant createTime;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;

}
