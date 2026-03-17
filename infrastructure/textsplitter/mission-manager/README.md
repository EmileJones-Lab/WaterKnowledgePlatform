# Mission Manager 任务管理模块

## 📌 模块简介

`mission-manager` 是水知识平台（WaterKnowledgePlatform）基础设施层中负责 **流水线任务状态管理** 与 **处理后文档持久化** 的核心模块。

该模块主要负责对文档处理流程中各个阶段（OCR、结构化抽取、向量化）的任务进行生命周期跟踪、状态记录以及结果存储。它衔接了领域层的仓储接口（Repository），为上层流水线提供了可靠的异步任务追踪能力。

## 🚀 核心功能

模块对外界（主要是 `domain` 层定义的接口）暴露了以下四大核心控制器（Repository 实现）：

1.  **OcrMissionController**: 管理 OCR 任务。记录源文档从初始状态到生成 Markdown/图片产物的全过程。
2.  **StructureExtractionMissionController**: 管理结构化抽取任务。记录将 OCR 产物解析并存入知识图谱（Neo4j）的过程。
3.  **EmbeddingMissionController**: 管理向量化任务。记录将图节点内容转换为向量并存入向量数据库（Milvus）的过程。
4.  **ProcessedDocumentController**: 管理 OCR 处理后的中间产物。负责文档元数据（MySQL）与物理文件（MinIO）的统一存取。

## 🏗 架构设计

模块遵循典型的 **领域驱动设计（DDD）基础设施层** 实现模式：

-   **Controller (Repository Impl)**: 实现了 `domain` 层定义的仓储接口，作为模块对外的唯一入口。
-   **Service 层**: 封装具体的业务逻辑，处理领域对象（Domain）与持久化对象（PO）之间的转换。
-   **Mapper 层**: 基于 MyBatis 实现，负责任务元数据在 MySQL 中的 CRUD 操作。
-   **Repository 层 (File Storage)**: 抽象了物理文件存储，目前已接入 **MinIO** 实现。

## 💾 存储方案

本模块采用“数据库 + 对象存储”的双重存储方案：

*   **MySQL**: 存储任务元数据（ID、状态、耗时、错误信息、关联关系等）。
    *   `gen_file_ocr`: OCR 任务表。
    *   `gen_file_extract`: 结构化抽取任务表。
    *   `gen_file_embed`: 向量化任务表。
    *   `processed_document`: 处理后文档元数据表。
*   **MinIO**: 存储 OCR 产出的物理文件（如 `.md` 文件、提取的图片等）。

## 📋 任务状态说明

任务状态（`MissionStatus`）统一遵循以下生命周期：
- `CREATED` (0): 任务已创建。
- `PENDING` (1): 等待执行。
- `RUNNING` (2): 正在执行中。
- `ERROR` (3): 执行失败，记录错误信息。
- `SUCCESS` (4): 执行成功，关联对应的产物 ID 或节点 ID。

---
*Author: Yeyezhi*
