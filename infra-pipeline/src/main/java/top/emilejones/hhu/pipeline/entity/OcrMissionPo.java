package top.emilejones.hhu.pipeline.entity;

import lombok.Data;
import top.emilejones.hhu.domain.pipeline.MissionStatus;

import java.time.Instant;


/**
 * OCR 任务持久化对象（PO）
 *
 * 表示一次对源文档执行 OCR 的异步任务实例，用于记录
 * OCR 任务的生命周期状态（创建、执行、完成/失败）以及
 * 其最终产出（ProcessedDocument）
 *
 * 该对象仅用于数据库持久化与查询，不包含具体 OCR 业务逻辑
 *
 * @author Yeyezhi
 */

@Data
public class OcrMissionPo {
    /** OCR 任务唯一标识（UUID） */
    private String ocrMissionId;

    /** 源文档 ID */
    private String sourceDocumentId;

    /** 任务状态（0=创建成功，1=运行中，2=任务失败，3=任务成功，4=等待中） */
    private MissionStatus statusType;

    /** OCR 成功后生成的 ProcessedDocument ID */
    private String processedDocumentId;

    /** 失败原因 */
    private String errorMessage;

    /** 创建时间 */
    private Instant createTime;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;
}
