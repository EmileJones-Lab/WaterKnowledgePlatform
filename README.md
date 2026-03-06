# 知识平台 - RAG 处理服务

本服务是研究生组内知识平台的 **RAG (Retrieval-Augmented Generation) 核心模块**。在组内的微服务架构中，本服务专注于将原始 PDF 文件转化为高质量的可检索向量知识库。

## 🚀 服务定位
本服务负责执行端到端的文档预处理流水线：从 **PDF 解析** 到 **层次化知识切分**，再到 **向量化 (Embedding)** 与 **多模态存储**（向量、图、关系型）。

### 核心特性：层次化结构切分 (Hierarchical Splitting)
与传统的固定长度切分不同，本项目利用文档的物理与逻辑结构（标题层级、段落、表格）进行切分。
- **语义完整性**：保留父子节点关系（如段落属于特定的 H3 小节）。
- **上下文回溯**：在召回阶段可以根据树状结构向上回溯完整的章节背景。
- **表格支持**：专门处理 PDF 中的表格解析与结构化存储。

## 🛠 技术栈
*   **后端框架**: Spring Boot 3.5.5, Kotlin 2.1
*   **向量数据库**: Milvus (存储 Embedding 向量)
*   **图数据库**: Neo4j (存储文档层级树状结构)
*   **文件存储**: MinIO (存储原始 PDF 及解析中间件)
*   **解析引擎**: MinerU (高保真 PDF 解析)
*   **数据库**: MySQL (任务状态管理与元数据存储)

## ⚙️ 环境配置要求
请参考以下 YAML 格式将配置写入到你的项目中。

```yaml
app:
  milvus:
    database: <your_milvus_database_name>
    collection: <your_milvus_collection_name>
    host: <milvus_host_ip_or_domain>
    port: <milvus_port>
  neo4j:
    host: <neo4j_host_ip_or_domain>
    port: <neo4j_port>
    user: <neo4j_username>
    password: <neo4j_password>
    database: <neo4j_database_name>
  model:
    host: <model_service_host_ip>
    port: <model_service_port>
    embeddingModel: <embedding_model_name>
    rerankModel: <rerank_model_name>
    dimension: <vector_dimension>
  rag:
    maxSentenceLength: <max_sentence_length>
    maxTableLength: <max_table_length>
    recallNumber: <recall_top_k_number>
  mysql:
    host: <mysql_host_ip_or_domain>
    port: <mysql_port>
    user: <mysql_username>
    password: <mysql_password>
    database: <mysql_database_name>
  minerU:
    host: <mineru_host_ip_or_domain>
    port: <mineru_port>
  minio:
    host: <minio_host_ip_or_domain>
    port: <minio_port>
    user: <minio_username>
    password: <minio_password>
```

## 📦 模块职责说明
- `domain`: 负责封装不涉及具体技术实现的核心业务逻辑与状态。
- `application`: 负责编排领域对象和基础设施服务以执行具体的业务用例。
- `infra-document`: 封装与原始文档管理模块的交互，负责获取原始文件的相关信息。
- `infra-textsplitter`: **核心算法模块**，实现基于 Markdown/JSON 结构的层次化切分逻辑。
- `infra-pipeline`: 负责管理向量化任务、结构提取任务、OCR任务状态以及持久化。
- `infra-knowledge`: 负责管理向量化后的数据，用于分知识库召回。
- `infra-model`: 封装 Embedding 和 Rerank 模型服务的客户端请求。
- `controller-web`: 提供 REST 接口，用于任务触发与监控。
- `controller-mcp`: 提供基于 MCP 协议的接口，支持 AI 客户端（如 Claude）直接调用 RAG 能力。
- `controller-command`: 命令行工具，用于测试召回效果。

## 🚦 快速开始
1.  **数据库初始化**: 运行 `sql/init.sql`。
2.  **配置环境**: 配置文件放在 `controller-web/src/main/resources/` 下，按照上文的配置说明进行配置。
3.  **服务启动**:
    ```bash
    ./gradlew :controller-web:bootRun --args='--spring.profiles.active=<YOUR_PROFILE_NAME>'
    ```
4.  **接口文档**: 接口文档使用springdoc，地址自行在`controller-web/src/main/resources/`下配置。
