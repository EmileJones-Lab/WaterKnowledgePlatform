# infrastructure-knowledge 模块

## 概述
`infrastructure-knowledge` 是知识库领域的底层基础设施实现模块。它主要负责实现 `domain` 模块中定义的 `KnowledgeCatalogRepository` 和 `KnowledgeDocumentRepository` 接口，通过 MyBatis 与关系型数据库交互，处理知识库（Catalog）与知识文档（Document）的持久化、检索及关联绑定逻辑。

## 核心职责

### 1. 仓储接口实现 (Repository Implementation)
该模块通过以下核心服务类实现了领域层的存储契约：

- **`KnowledgeCatalogServiceImpl`**: 
    - 实现 `KnowledgeCatalogRepository` 接口。
    - 负责知识库目录的 CRUD 操作（采用 **Upsert** 语义进行保存）。
    - 维护知识库与文档的 **多对多绑定关系**（通过 `collection_document` 关联表）。
    - **业务规则校验**：在绑定文档时，校验文档切分类型（如结构化切分、字符切分）与知识库类型是否匹配。
- **`KnowledgeDocumentServiceImpl`**:
    - 实现 `KnowledgeDocumentRepository` 接口。
    - 负责知识文档元数据的持久化及其与向量化任务（Embedding Mission）的关联。
    - 提供复杂的筛选逻辑，如查询特定知识库下的候选文档、带有绑定时间上下文的文档查询等。

### 2. 持久化策略
- **软删除 (Soft Delete)**: 模块内广泛使用 `isDelete` 字段（`DeleteConstant`）来标记记录状态，而非物理删除，以保证数据的可追溯性。
- **DTO 与 Domain 转换**: 利用 `DtoToDomainUtil` 工具类，在数据库 POJO/DTO 与领域模型（Aggregate Root）之间进行转换，确保领域层的纯洁性。

## 核心类说明

| 类名 | 说明 |
| :--- | :--- |
| `KnowledgeCatalogServiceImpl` | 处理知识库目录生命周期，包含分类检索、软删除及文档绑定逻辑。 |
| `KnowledgeDocumentServiceImpl` | 处理知识文档元数据，包含候选文档筛选及基于任务 ID 的反查。 |
| `KnowledgeCatalogMapper` | MyBatis Mapper，定义对 `col_knowledge_base` 表的 SQL 操作。 |
| `KnowledgeDocumentMapper` | MyBatis Mapper，定义对 `col_knowledge_document` 表的 SQL 操作。 |
| `CollectionDocumentMapper` | 处理知识库与文档关联表的中间逻辑（Bind/Unbind）。 |

## 技术栈
- **持久化框架**: MyBatis
- **数据库**: MySQL (关系型元数据存储)
- **工具**: Spring Boot, Java 17

## 业务逻辑约束
在 `KnowledgeCatalogServiceImpl` 中硬编码了类型匹配逻辑：
- **结构化知识库** (`STRUCTURE_KNOWLEDGE_DIR`)：仅允许绑定 **结构化切分** (`STRUCTURE_SPLITTER`) 的文档。
- **字符切分知识库** (`CHAR_NUMBER_SPLIT_DIR`)：仅允许绑定 **字符长度切分**（200/400/600字符）的文档。
