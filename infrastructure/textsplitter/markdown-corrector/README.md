# markdown-corrector 模块

`markdown-corrector` 是一个用于规范化和修正 Markdown 文档结构的模块。它主要通过扫描文档中的标题，构建层级结构树，并根据该树重新生成具有正确层级深度（即 `#` 数量）的 Markdown 文本。

## 核心功能

该模块目前的核心实现类为 `top.emilejones.hhu.preprocessing.structure.TitleTreeExtractor`。其主要功能包括：

1.  **文本预处理**：
    *   **目录移除**：自动识别并移除文档中的“目录”部分，避免其干扰结构解析。
    *   **标题规范化**：去除标题行起始多余的 `#` 号，确保标题标记（如 `#`）与内容之间有空格。
    *   **层级标题修正**：将类似 `1. 1. 1` 的层级标题规范化为 `1.1.1` 格式。
2.  **结构树构建**：
    *   扫描文档内容，识别各类标题（如数字序列、中文标题、Markdown 原生标题等）。
    *   基于标题的类型和出现顺序，构建一棵反映文档真实层级的 `Node` 树。
3.  **标题层级修正**：
    *   根据生成的结构树，自动计算每个标题应有的深度。
    *   重新生成文档，并按照深度自动填充对应数量的 `#`（例如：一级标题固定为 `# `，其余标题根据其在树中的深度确定 `#` 数量）。
4.  **结构可视化**：
    *   提供 `printStructureTree` 方法，可以在控制台以树状图形式直观展示提取出的文档结构。

## 主要类说明

- **`TitleTreeExtractor`**: 核心逻辑实现类，负责预处理、树构建和文本修正。
- **`AbstractTitleTreeExtractor`**: 定义了提取流程的抽象基类，规定了预处理、判定标题、构建树和修正文本的标准步骤。
- **`top.emilejones.hhu.preprocessing.structure.tree.Node`**: 结构树的节点类，存储标题的行号、类型及父子关系。
- **`top.emilejones.hhu.preprocessing.structure.enums.TitleType`**: 定义了多种标题匹配模式（正则）。

## 使用示例

```java
TitleTreeExtractor extractor = new TitleTreeExtractor();
// 修正 Markdown 文本并获取结果
String correctedText = extractor.extract(originalMarkdown);
// 在控制台打印文档结构树
extractor.printTitleLevel(originalMarkdown);
```

## 注意事项与未来改进

- **OCR 扫描识别问题**：如果输入的 Markdown 文本是经由 OCR 扫描生成的，由于 OCR 的精度限制（如错别字、标题格式丢失或识别错误），可能会导致扫描出的结构树比较混乱。
- **AI 辅助解析（设想）**：目前的修正逻辑完全基于规则和正则表达式来生成结构树。为了获得更精确的解析效果，未来可以尝试接入 AI 大模型，让 AI 对初步生成的结构树进行逻辑校验和调整，修正因规则失效或 OCR 错误导致的层级混乱，从而生成更准确的文档结构。

## 项目状态

本模块中除 `TitleTreeExtractor.java` 及其相关核心类外，其他早期尝试解决问题的类目前基本已舍弃。该类是目前处理 Markdown 文档结构规范化的主要工具。
