# Structure Extraction Module

## 概述
`structure-extraction` 模块负责将 Markdown 格式的文本内容解析为结构化的树状模型（Heading Tree），并将其存储到 Neo4j 图数据库中。该模块不仅保留了文档的层级结构（标题-子标题-正文），还建立了节点间的序列关系（前驱-后继），为后续的知识检索（RAG）提供丰富的上下文语义。

## 核心功能
1.  **Markdown 结构解析**：通过 `MarkdownStructureParser` 将 Markdown 文本解析为以 `FileNode` 为根，`TextNode` 为分支与叶子的树状结构。
2.  **树形结构持久化**：利用 Neo4j 存储文档树，建立包含、父子、先后序等多种图关系。
3.  **智能文本切分**：
    *   **句子切分**：根据标点符号（如 `。` `；` `!`）对超长正文进行切分。
    *   **表格切分**：支持 HTML 表格解析，并能将其转换为 CSV 格式或根据长度限制切分为多个带表头的子表。
4.  **知识召回（Recall）**：结合 Milvus 向量搜索与 Neo4j 拓扑结构，提供多策略的知识召回能力。
5.  **OCR 集成**：集成 MinerU 客户端，支持通过 OCR 服务将原始文档转换为可解析的 Markdown。

## 技术栈
*   **语言**: Kotlin
*   **框架**: Spring Boot
*   **数据库**: Neo4j (图存储), Milvus (向量存储)
*   **工具**: Jsoup (HTML/表格处理), OkHttp (网络请求)

## 数据模型 (Neo4j)

### 节点标签
*   `FileNode`: 代表一个完整的文档。
*   `TextNode`: 代表文档中的一个片段（标题、正文、表格、图片）。

### 关系类型
*   `PARENT` / `CHILD`: 描述文档的层级嵌套结构（如 H1 包含 H2）。
*   `PRE_SEQUENCE` / `NEXT_SEQUENCE`: 描述文档的线性阅读顺序。
*   `CONTAIN`: `FileNode` 与其下所有 `TextNode` 的包含关系。

### 属性说明
*   `id`: 内部唯一 ID。
*   `text`: 文本片段内容。
*   `type`: 节点类型（TITLE, COMMON_TEXT, TABLE, IMAGE, NULL）。
*   `level`: 标题层级（1-6），正文通常为 `Int.MAX_VALUE`。
*   `seq`: 节点在全局文档中的物理顺序索引。
*   `vector`: 节点的向量嵌入（存储为浮点数组）。

## 核心组件说明

| 组件名称 | 说明 |
| :--- | :--- |
| `MarkdownStructureParser` | 递归解析 Markdown 行，构建初始树结构。 |
| `SplitTextNodeTool` | 负责对树中的叶子节点执行长度检查并进行物理切分。 |
| `Neo4jRepository` | 封装了复杂的 Cypher 查询，处理树的插入、节点移动及上下文检索。 |
| `RagToolsAdaptor` | 领域层适配器，统一了 OCR、结构提取、向量化与存储的生命周期。 |
| `RecallService` | 召回引擎，实现向量检索后的二次重排及图路径扩展。 |
