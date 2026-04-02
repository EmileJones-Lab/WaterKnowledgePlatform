# infrastructure-document 模块

## 概述
`infrastructure-document` 是原始文档领域的底层基础设施实现模块。它主要负责实现 `domain` 模块中定义的 `SourceDocumentRepository` 接口，提供对原始上传文档（PDF, DOCX, TXT 等）的元数据查询及二进制内容流（InputStream）的读取能力。

## 核心实现：MySQL + MinIO 混合存储
该模块采用了“元数据与内容分离”的存储策略：
1.  **元数据存储 (MySQL)**：通过 `SourceDocumentMapper` 访问数据库，管理文件的基本信息（文件名、类型、路径、归属目录等）。
2.  **内容存储 (MinIO)**：利用 MinIO 对象存储服务保存真实的物理文件，通过 `MinioClient` 获取文件的输入流。

## 主要类说明

### 1. 仓储接口实现 (Repository Implementation)
- **`SourceDocumentServiceImpl`**:
    - 实现 `SourceDocumentRepository` 接口。
    - **元数据查询**：根据文件 ID 从 MySQL 查询 `SourceDocumentPO` 并转换为领域对象 `SourceDocument`。
    - **内容读取**：解析 MinIO 的路径格式（形如 `http://{host}/{bucket}/{objectKey}`），提取 Bucket 和 ObjectKey 后，从 MinIO 获取 `InputStream`。
    - **路径处理**：自动处理完整的 URL，并将其提取为供领域层使用的相对路径。

### 2. 数据映射与持久化
- **`SourceDocumentMapper`**: MyBatis Mapper 接口，定义了对 `col_file` 表的操作。
- **`SourceDocumentPO`**: 数据库持久化对象，对应 `col_file` 表的结构。

## 核心契约与逻辑
1.  **依赖倒置 (DIP)**：该模块位于基础设施层，被动实现领域层的接口，确保业务逻辑不直接依赖于 MinIO 或 MySQL 的 SDK。
2.  **类型映射**：在将数据库记录转换为领域对象时，会将字符串类型的 `filetype` 映射为领域层的 `SourceFileType` 枚举。
3.  **路径解析逻辑**：
    - 输入路径示例：`/bamboo/2024/11/27/xxx.docx`
    - 解析结果：Bucket = `bamboo`, ObjectKey = `2024/11/27/xxx.docx`

## 技术栈
- **对象存储**: MinIO SDK (Java)
- **持久化框架**: MyBatis
- **数据库**: MySQL
- **工具**: Spring Boot, Java 17

## 配置要求
使用该模块需要正确配置 MinIO 相关的连接参数（Endpoint, AccessKey, SecretKey），这些配置通常由 `infrastructure-environment` 模块或 Spring Boot 配置文件提供。
