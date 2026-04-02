# Structure Extraction Module

## 概述
`structure-extraction` 模块负责将 Markdown 格式的文本内容解析为结构化的树状模型（Heading Tree），并将其存储到 Neo4j 图数据库中。该模块不仅保留了文档的层级结构（标题-子标题-正文），还建立了节点间的序列关系（前驱-后继），并提供了丰富的预处理能力（摘要生成、层级修正、物理切分），为后续的知识检索（RAG）提供高价值的上下文语义。

## 核心功能
1.  **Markdown 结构解析**：通过 `MarkdownStructureParser` 将 Markdown 文本解析为以 `FileNode` 为根，`TextNode` 为分支与叶子的树状结构。
2.  **树形结构精加工 (Preprocessor)**：在初始树构建完成后，对其进行多轮处理，包括语义摘要提取、叶子节点规范化、长文本物理切分等。
3.  **图数据库持久化**：利用 Neo4j 存储文档树，建立包含（CONTAIN）、父子（PARENT/CHILD）、先后序（PRE/NEXT_SEQUENCE）等多种拓扑关系。
4.  **智能文本切分**：
    *   **句子切分**：根据标点符号对超长正文进行递归切分，确保每个分片符合 LLM 上下文长度。
    *   **表格切分**：将 HTML 表格转换为 CSV 格式，并根据长度限制切分为带表头的子表。
5.  **知识召回与 OCR**：集成向量搜索与图路径扩展，并支持通过 OCR 服务（如 MinerU）处理原始文档。

## 数据模型 (Neo4j)

### 节点属性说明
#### FileNode (文档根节点)
*   `id`: 内部唯一 ID。
*   `fileId`: 外部文件关联 ID。
*   `isEmbedded`: 是否已完成向量化嵌入。
*   `fileAbstract`: **(New)** AI 生成的整篇文档摘要。
*   `isDelete`: 逻辑删除标记。

#### TextNode (内容节点)
*   `id`: 内部唯一 ID。
*   `text`: 文本片段内容。
*   `type`: 节点类型（TITLE, COMMON_TEXT, TABLE, IMAGE, NULL）。
*   `level`: 标题层级（1-6），叶子节点/正文通常为 `Int.MAX_VALUE`。
*   `seq`: 节点在全局文档中的物理顺序索引。
*   `summary`: **(New)** AI 为该节点（及子树）生成的语义摘要。
*   `vector`: 节点的向量嵌入。
*   `isDelete`: 逻辑删除标记。

### 关系类型
*   `PARENT` / `CHILD`: 描述文档的标题嵌套结构（如 H1 包含 H2）。
*   `PRE_SEQUENCE` / `NEXT_SEQUENCE`: 描述文档的线性阅读顺序（忽略层级）。
*   `CONTAIN`: `FileNode` 与其下所有 `TextNode` 的归属关系。

## 预处理器 (Preprocessor) 任务说明
`top.emilejones.hhu.textsplitter.preprocessor` 包负责对初步生成的树进行后续加工，使其更符合向量检索和 LLM 的需求：

| 处理器名称 | 任务描述 |
| :--- | :--- |
| `TextNodeSummaryProcessor` | **摘要生成**：采用自底向上（后序遍历）的方式，利用 AI 为每个节点生成摘要。中间节点的摘要会参考其子节点的摘要列表，最终将整棵树的根摘要同步到 `FileNode` 的 `fileAbstract`。 |
| `TextNodeLeafLevelProcessor` | **层级规范化**：识别所有子节点均为叶子的节点，并将其子节点的 `level` 统一修正为 `Int.MAX_VALUE`，移除标题符号（如 `#`），确保叶子节点在图中具有一致的表示。 |
| `SplitTextNodeTool` | **物理切分**：对长度超过阈值的文本节点和表格进行“切分重绑”。它会维持原有的父子和序列关系，并在切分后重新校准全局 `seq` 顺序。 |

## 核心组件说明

| 组件名称 | 说明 |
| :--- | :--- |
| `MarkdownStructureParser` | 递归解析 Markdown 行，构建初始的 `TextNode` 树结构。 |
| `Neo4jRepository` | 封装 Cypher 查询，处理树的原子化插入、节点移动及基于路径的上下文检索。 |
| `RagToolsAdaptor` | 领域层适配器，编排 OCR -> 结构解析 -> 预处理 -> 存储的全生命周期。 |
| `RecallService` | 召回引擎，实现 Milvus 向量检索后的 Neo4j 图拓扑扩展（如找回父节点或上下文片段）。 |
