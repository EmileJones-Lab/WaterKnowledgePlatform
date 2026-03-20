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
    database: <database_name>           # [必填] Milvus 数据库名
    host: <host>                        # [必填] Milvus 地址
    port: <port>                        # [必填] Milvus 端口
  neo4j:
    host: <host>                        # [必填] Neo4j 地址
    port: <port>                        # [必填] Neo4j 端口
    user: <username>                    # [必填] Neo4j 用户名
    password: <password>                # [必填] Neo4j 密码
    database: <database_name>           # [必填] Neo4j 数据库名
  model:
    embeddingUrl: <url>                 # [必填] Embedding API 地址 (会自动补齐 /embeddings)
    rerankUrl: <url>                    # [必填] Rerank API 地址 (会自动补齐 /rerank)
    token: null                         # [选填] 全局 API Key，默认 null
    embeddingToken: null                # [选填] Embedding 专用 Key，默认继承全局 token
    rerankToken: null                   # [选填] Rerank 专用 Key，默认继承全局 token
    embeddingModel: <name>              # [必填] Embedding 模型名称
    rerankModel: <name>                 # [必填] Rerank 模型名称
    dimension: <int>                    # [必填] 向量维度
  rag:
    maxSentenceLength: <int>            # [必填] 文本 chunk 最大长度
    maxTableLength: <int>               # [必填] 表格 chunk 最大长度
    recallNumber: <int>                 # [必填] 召回数量 (Top K)
  mysql:
    host: <host>                        # [必填] MySQL 地址
    port: <port>                        # [必填] MySQL 端口
    user: <username>                    # [必填] MySQL 用户名
    password: <password>                # [必填] MySQL 密码
    database: <database_name>           # [必填] MySQL 数据库名
  minerU:
    host: <host>                        # [必填] MinerU 地址
    port: <port>                        # [必填] MinerU 端口
    connectTimeout: 60                  # [选填] 连接超时 (秒)，默认 60
    writeTimeout: 120                   # [选填] 写入超时 (秒)，默认 120
    readTimeout: 0                      # [选填] 读取超时 (秒)，0 为无限，默认 0
    outputDir: "./output"               # [选填] 结果暂存路径，默认 "./output"
    langList: ["ch"]                    # [选填] 语言列表，默认 ["ch"]
    backend: "pipeline"                 # [选填] 后端引擎，默认 "pipeline"
    parseMethod: "auto"                 # [选填] 解析模式，默认 "auto"
    formulaEnable: true                 # [选填] 解析公式，默认 true
    tableEnable: true                   # [选填] 解析表格，默认 true
    returnMd: true                      # [选填] 返回 Markdown，默认 true
    returnMiddleJson: false             # [选填] 返回中间过程 JSON，默认 false
    returnModelOutput: false            # [选填] 返回原始输出，默认 false
    returnContentList: true             # [选填] 返回内容列表，默认 true
    returnImages: true                  # [选填] 提取图片，默认 true
    responseFormatZip: false            # [选填] 响应压缩为 ZIP，默认 false
    startPageId: 0                      # [选填] 起始页码，默认 0
    endPageId: 99999                    # [选填] 结束页码，默认 99999
  minio:
    host: <host>                        # [必填] MinIO 地址
    port: <port>                        # [必填] MinIO 端口
    user: <username>                    # [必填] MinIO 用户名
    password: <password>                # [必填] MinIO 密码
```

## 📦 模块职责说明
- `domain`: 核心业务逻辑与状态建模（不依赖具体技术栈）。
- `application-service`: 业务用例编排中心。
    - `platform`: 平台核心业务逻辑编排。
    - `command`: 测试用的命令行工具服务编排。
    - `configuration`: 服务编排配置管理。
- `infrastructure`: 基础设施实现层。
    - `environment`: 统一管理应用的外部环境配置，并负责将这些基础设施的服务客户端及其对应的配置属性注入为 Spring Bean。
    - `document`: 原始文档管理与元数据交互。
    - `textsplitter`: **核心算法模块**。
        - `structure-extraction`: 文本结构提取与解析。
        - `markdown-corrector`: Markdown 格式自动修正。
        - `mission-manager`: 切分任务的状态流转与管理。
    - `knowledge`: 向量化数据的持久化与多维召回管理。
    - `model`: 封装 Embedding 与 Rerank 模型服务的客户端请求。
    - `qa`: 问答系统相关逻辑支持。
- `controller`: 多端接入层。
    - `web`: 标准 RESTful API，用于任务触发与监控。
    - `mcp`: 基于 MCP 协议的接口，支持 AI 客户端直接调用。
    - `command`: 命令行工具，用于本地测试召回效果。

## 🚦 快速开始
1.  **数据库初始化**: 将 `sql/init.sql` 导入到你的 MySQL 数据库中。
2.  **环境配置**: 
    - 准备配置文件 `application-<PROFILE>.yml`。
    - 放置路径：`controller/web/src/main/resources/` 或 Jar 包同级目录。
3.  **启动服务**:
    - **方式一：通过 Gradle 直接启动（推荐开发使用）**
      ```bash
      ./gradlew :controller:web:bootRun --args='--spring.profiles.active=<PROFILE>'
      ```
    - **方式二：打包后通过 Jar 启动**
      1. 编译打包：
         ```bash
         ./gradlew :controller:web:bootJar
         ```
      2. 运行 Jar：
         ```bash
         java -jar controller/web/build/libs/web.jar --spring.profiles.active=<PROFILE>
         ```
    - **方式三：通过 Docker 镜像启动**
      1. 构建镜像：
         ```bash
         docker build -t structure-text-splitter .
         ```
      2. 运行容器：
         ```bash
         docker run -d \
           -p <YOUR_PORT>:8080 \
           -e "SPRING_PROFILES_ACTIVE=<PROFILE>" \
           --name <CONTAINER_NAME> \
           structure-text-splitter
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
- **Swagger UI (交互式文档)**: `http://<YOUR_HOST>:<YOUR_PORT>/open-api/index.html`
- **OpenAPI JSON**: `http://<YOUR_HOST>:<YOUR_PORT>/open-api/json`
