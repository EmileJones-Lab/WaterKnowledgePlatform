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
    token: <your_api_token>                # OpenAI 格式服务的 API Key (可选)
    embeddingModel: <embedding_model_name> # Embedding 模型名称 (OpenAI 格式)
    rerankModel: <rerank_model_name>       # Rerank 模型名称
    dimension: <vector_dimension>          # Embedding 模型生成的向量维度 (需与 Milvus 集合维度一致)
  rag:
    maxSentenceLength: <max_sentence_length> # 文本 chunk 的最大长度
    maxTableLength: <max_table_length>       # 表格 chunk 的最大长度
    recallNumber: <recall_top_k_number>      # 召回前几条数据（Top K）
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
- `domain`: 核心业务逻辑与状态建模（不依赖具体技术栈）。
- `application-service`: 业务用例编排中心。
    - `platform`: 平台核心业务逻辑编排。
    - `command`: 测试用的命令行工具服务编排。
    - `configuration`: 服务编排配置管理。
- `infrastructure`: 基础设施实现层。
    - `document`: 原始文档管理与元数据交互。
    - `textsplitter`: **核心算法模块**。
        - `structure-extraction`: 文本结构提取与解析。
        - `markdown-corrector`: Markdown 格式自动修正。
        - `mission-manager`: 切分任务的状态流转与管理。
    - `knowledge`: 向量化数据的持久化与多维召回管理。
    - `model`: 封装 Embedding 与 Rerank 模型服务的客户端请求。
    - `qa`: 问答系统相关逻辑支持。
- `controller`: 多端接入层。
    - `controller-web`: 标准 RESTful API，用于任务触发与监控。
    - `controller-mcp`: 基于 MCP 协议的接口，支持 AI 客户端直接调用。
    - `controller-command`: 命令行工具，用于本地测试召回效果。

## 🚦 快速开始
1.  **数据库初始化**: 将 `sql/init.sql` 导入到你的 MySQL 数据库中。
2.  **环境配置**: 
    - 准备配置文件 `application-<PROFILE>.yml`。
    - 放置路径：`controller/controller-web/src/main/resources/` 或 Jar 包同级目录。
3.  **启动服务**:
    - **方式一：通过 Gradle 直接启动（推荐开发使用）**
      ```bash
      ./gradlew :controller:controller-web:bootRun --args='--spring.profiles.active=<PROFILE>'
      ```
    - **方式二：打包后通过 Jar 启动**
      1. 编译打包：
         ```bash
         ./gradlew :controller:controller-web:bootJar
         ```
      2. 运行 Jar：
         ```bash
         java -jar controller/controller-web/build/libs/controller-web.jar --spring.profiles.active=<PROFILE>
         ```

## 📖 接口文档
本服务使用 `springdoc` 自动生成接口文档。

### 1. 开启配置
在你的 `application-<PROFILE>.yml` 中添加以下配置：
```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

### 2. 访问地址
- **Swagger UI (交互式文档)**: `http://<YOUR_HOST>:<YOUR_PORT>/open-api/swagger-ui/index.html`
- **OpenAPI JSON**: `http://<YOUR_HOST>:<YOUR_PORT>/open-api/swagger-ui/json`
