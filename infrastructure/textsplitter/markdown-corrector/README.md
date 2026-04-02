# markdown-corrector 模块

`markdown-corrector` 是一个用于规范化和修正 Markdown 文档结构的模块。它主要通过扫描文档中的标题，构建层级结构树，并根据该树重新生成具有正确层级深度（即 `#` 数量）的 Markdown 文本。

## 核心功能

该模块支持基于规则的解析以及 AI 辅助的语义解析：

1.  **文本预处理与修复**：
    *   **基础修复**：自动识别并移除目录、规范化标题空格、修正 `1. 1. 1` 式的编号。
    *   **AI OCR 修复**：利用大模型修复 OCR 识别出的标题粘连（如 `2.3.2标题（1）内容` 拆分为多行）、补全缺失空格等。
2.  **结构树构建**：
    *   扫描文档内容，识别各类标题（如数字序列、中文标题、Markdown 原生标题等）。
    *   基于标题的类型和出现顺序，构建反映文档层级的 `Node` 树。
3.  **AI 语义纠偏**：
    *   针对复杂的异构序号体系（如同时存在 `2.3.1`、`（一）`、`1)`），利用 AI 分析语义逻辑。
    *   强制约束：AI 根据编号深度映射和上下文语义，对初步生成的结构树进行层级（Level）修正。
4.  **层级修正与生成**：
    *   根据最终确定的结构树，自动计算标题深度并填充对应数量的 `#`。
5.  **结构可视化**：
    *   提供 `printStructureTree` 方法，在控制台以树状图形式直观展示文档结构。

## 主要类说明

- **`TitleTreeExtractor`**: 基础实现类，完全基于预设正则表达式和位置规则进行解析。
- **`TitleTreeExtractorWithAI`**: **装饰器类**，在基础解析之上接入 AI 能力，负责 OCR 文本修复和层级语义纠偏。
- **`AbstractTitleTreeExtractor`**: 定义提取流程的抽象基类。
- **`top.emilejones.hhu.preprocessing.structure.tree.Node`**: 结构树节点类。
- **`top.emilejones.hhu.preprocessing.structure.enums.TitleType`**: 定义标题匹配模式（正则）。

## 使用示例

### 基础模式（基于规则）
```java
TitleTreeExtractor extractor = new TitleTreeExtractor();
String correctedText = extractor.extract(originalMarkdown);
```

### AI 增强模式（推荐用于 OCR 文本）
```java
// 需要提供 ModelClient 实现
ModelClient modelClient = new YourModelClientImpl(); 
TitleTreeExtractorWithAI aiExtractor = new TitleTreeExtractorWithAI(new TitleTreeExtractor(), modelClient);

// AI 会先进行文本修复，再进行语义层级纠正
String correctedText = aiExtractor.extract(originalMarkdown);
// 打印经过 AI 修正后的结构树
aiExtractor.printTitleLevel(originalMarkdown);
```

## AI 辅助逻辑详解

`TitleTreeExtractorWithAI` 采用了两阶段 AI 处理：

1.  **第一阶段：文本清洗 (Rewrite)**
    *   **Prompt 策略**：定位为“OCR 文本修复专家”。
    *   **目标**：解决标题与正文粘连、多个编号挤在同一行的问题。
    *   **输出**：严格的 JSON 格式，包含修复后的文本片段（Chunks）。

2.  **第二阶段：结构纠偏 (Rebuild)**
    *   **Prompt 策略**：定位为“文档结构分析专家”。
    *   **目标**：处理“显性序号深度映射”和“异构序号平级兼容”。
    *   **纠错能力**：例如，若 `2.3.2` 为 Level 1，AI 会确保其后的 `2.3.2.1` 严格映射为 Level 2，即使正则表达式初步判定有误。

## 项目状态

目前 `TitleTreeExtractorWithAI` 是处理复杂或 OCR 生成的 Markdown 文档的核心推荐工具。它解决了传统规则解析难以应对的语义逻辑问题，使层级修正更加智能化。
