# Gemini AI 辅助开发指南 (Project Context & AI Rules)

## 1. 项目概述 (Project Overview)
- **项目类型**: 多模块 后端应用
- **主要语言**: Kotlin & Java
- **构建系统**: Gradle(Kotlin)
- **核心框架**: Spring Boot 3.5.5

## 2. 核心交互指令 (AI Behavior Rules)
作为本项目的 AI 编码助手，在生成代码或回答问题时，请严格遵守以下约束：
- **保持客观中立**：避免使用自夸或修饰性词汇（如“完美的”、“令人惊叹的”）。
- **开门见山**：拒绝废话开场白，直接给出代码、配置或解释。
- **事实驱动**：优先提供具体的数据、代码片段、案例或逻辑推导。
- **言简意赅**：使用清晰、专业的短句。代码注释需准确描述“为什么”而不是“是什么”。
- **语言偏好**：默认使用中文回答解释，代码中变量和方法保持英文命名，注释使用中文。

## 3. 技术栈与构建规范 (Build & Dependency)
- **Gradle 构建**:
    - 必须使用 Kotlin DSL (`*.gradle.kts`) 语法生成构建配置。
    - 优先使用**版本目录 (Version Catalogs)** (`gradle/libs.versions.toml`) 管理依赖。
    - 对于多模块项目，遵循公共配置抽取（如使用 `buildSrc` 或 `convention plugins`），避免在子模块中重复写配置。
- **依赖引用**: 提供依赖建议时，请给出 `libs.versions.toml` 的配置项以及 `build.gradle.kts` 中的 `implementation(libs.xxx)` 写法。

## 4. Java & Kotlin 混编规范 (Interop Guidelines)
本项目包含 Java 和 Kotlin 代码，生成代码时必须处理好两者的互操作性（Interoperability）：

- **语言选择**:
    - **新功能/新模块**：根据当前路径判断，例如在`src/*/java/**`则使用Java语言，在`src/*/kotlin/**`则使用Kotlin语言
    - **修改旧代码**：保持原有语言，除非明确要求重构为 Kotlin。
- **Null 安全 (Nullability)**:
    - 在 Java 代码中声明方法返回值和参数时，必须使用 `@Nullable` 或 `@NotNull` 注解（如 `org.jetbrains.annotations` 或 `jakarta.annotation`），以便 Kotlin 代码能正确推断非空性。
- **Kotlin 供 Java 调用**:
    - 需要在 Java 中使用的 Kotlin 方法，如果包含默认参数，必须添加 `@JvmOverloads` 注解。
    - 需要在 Java 中作为静态方法调用的 Kotlin 伴生对象（`companion object`）方法，必须添加 `@JvmStatic` 注解。
    - 处理好异常抛出，对 Java 调用的方法按需添加 `@Throws` 注解。
- **集合处理**: 注意 Java `List` 到 Kotlin `List`（不可变）和 `MutableList`（可变）的映射关系。

## 5. 编码风格与最佳实践 (Coding Standards)
- **Kotlin**:
    - 优先使用数据类 (`data class`) 替代普通的 POJO。
    - 充分利用扩展函数 (Extension Functions) 来保持代码整洁。
    - 优先使用 `val` 声明不可变变量，尽量减少 `var` 的使用。
    - 复杂的并发逻辑优先考虑协程 (`Coroutines`) 而不是底层线程或 Java `CompletableFuture`。
- **Java**:
    - 遵循现代 Java 规范（如适用 Java 17/21，使用 `record`、`var`、`switch` 表达式等）。
    - 使用 `Optional` 处理可能为空的返回值。
- **架构约定**:
    - 遵循领域驱动设计 (DDD) 或清晰的按层架构（Controller层、Service层、Repository/Infrastructure层）。
    - 保持实体 (Entity) 的纯洁性，将业务逻辑封装在 Service 或 Domain 层。
    - 实现一个方法时，可以尽量拆分为多个子方法，做到代码即文档。

## 6. 文档规范
- **类文档**：所有类必须包含文档，说明其核心职责、常用方法及使用指引。
- **方法文档**：所有 `public` 方法必须提供文档，明确描述方法用途、参数含义及返回值。
- **内容约束**：文档需保持精简。仅描述“功能与用法”（What & How to use），严禁涉及具体的“内部实现逻辑”（How it works）。

## 7. 常见规避点 (Anti-Patterns to Avoid)
- **不要**在 Kotlin 代码中使用 `!!` 操作符，请使用安全调用 `?.`、Elvis 操作符 `?:` 或显式的空值检查。
- **不要**在子模块的 `build.gradle.kts` 中写死版本号，必须通过外部统一管理。
- **不要**生成过时 (Deprecated) 的 API 替代方案，始终使用最新的稳定版 API。
- **不要**在用户没有明确要求生成测试类的时候，自己生成测试类。