# domain 模块

## 概述
`domain` 模块是项目的核心领域层，遵循 **领域驱动设计 (DDD)** 原则。它包含了纯粹的业务实体（Entities）、聚合根（Aggregate Roots）、领域事件（Domain Events）以及仓储接口（Repositories）和领域服务（Domain Services）。

该模块不依赖于任何具体的外部技术实现（如数据库、中间件），通过依赖倒置原则（DIP）定义契约，确保业务逻辑的稳定性与可测试性。

## 目录结构与领域模型

### 1. 基础抽象 (Core Abstractions)
定义了 DDD 开发的基础设施：
- `AggregateRoot`: 基础聚合根，支持唯一标识管理及领域事件的收集与推送。
- `DomainEvent`: 领域事件基类，记录事件发生的时刻。
- `framework`: 通用的数据处理器接口（`ConsistentDataProcessor`, `ConsistentBatchProcessor`），定义了保存、查询、批量操作的标准契约。

### 2. 文档领域 (Document Domain)
管理原始文件的元数据与生命周期：
- `SourceDocument`: 描述用户上传的原始文件（PDF, DOCX 等）。
- `Catalog`: 原始文件的组织目录。
- `repository/SourceDocumentRepository`: 抽象了源文件的持久化及内容流（InputStream）的读取能力。

### 3. 知识库领域 (Knowledge Domain)
管理向量化后的知识资产及其组织形式：
- `KnowledgeCatalog`: 知识库目录，对应向量数据库中的 Collection。
- `KnowledgeDocument`: 知识库内的文档元数据，关联具体的向量化任务。
- `KnowledgeDomainService`: 跨聚合领域服务，负责校验知识库与文档类型的匹配逻辑（如结构化知识库仅能绑定结构化文档）。
- `event/`: 包含知识库创建、文档绑定等核心领域事件。

### 4. 流水线领域 (Pipeline Domain)
负责文档处理全流程（OCR -> 结构抽取 -> 向量化）的状态机管理与外部能力抽象：

#### 任务聚合根
- `OcrMission`: 管理 OCR 识别任务的状态（PENDING, RUNNING, SUCCESS, ERROR）。
- `StructureExtractionMission`: 管理文档结构切分任务。
- `EmbeddingMission`: 管理文本向量化及入库任务。

#### 能力网关 (Gateways)
定义了对外部系统的调用契约：
- `OcrGateway`: 抽象 OCR 能力（如 MinerU），处理文件流并返回 Markdown 与图片。
- `StructureExtractionGateway`: 抽象结构解析能力，接收 Markdown 二进制内容并将其转换为树状节点。
- `EmbeddingGateway`: 抽象向量模型调用。
- `VectorRepository`: 抽象向量数据库存储。

### 5. 处理结果领域 (Result Domain)
描述文档处理后的最终产物：
- `ProcessedDocument`: OCR 处理后生成的中间态 Markdown 文档。
- `FileNode`: 文档在知识图谱/树状结构中的根节点。
- `TextNode`: 最小的内容单元（标题、正文、表格、图片），承载了层级（level）、顺序（seq）及向量（vector）信息。
- `MissionStatus`: 统一的任务状态枚举。

## 核心设计契约
1. **状态机驱动**: 所有任务（Mission）均通过聚合根内部的方法（如 `start()`, `success()`, `failure()`）进行状态流转，并严格校验前提条件。
2. **依赖倒置**: 所有的 Repository 和 Gateway 仅定义接口。基础设施层（Infrastructure）负责具体实现（如 Neo4j, Milvus, MySQL），确保领域层不被技术细节污染。
3. **向量化处理**: `TextNode` 节点在 `saveVector` 时会进行状态检查，确保向量数据的不可变性与一致性。
