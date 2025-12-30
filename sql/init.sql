CREATE TABLE `gen_file_ocr` (
  `ocr_mission_id` CHAR(36) NOT NULL COMMENT '主键，OCR 任务唯一 ID（UUID）',
  `file_id` CHAR(36) NOT NULL COMMENT '源文件id',
  `status_type` TINYINT NOT NULL COMMENT '任务状态（0=创建成功；1=运行中；2=任务失败；3=任务成功；4=等待中）',
  `processed_doc_id` CHAR(36) NULL COMMENT 'ocr成功后生成的ProcessedDocument ID（此处存储success的信息可扩展）',
  `error_message` VARCHAR(1024) NULL COMMENT 'ocr失败的原因说明',
  `create_time` DATETIME NULL COMMENT 'ocr任务创建时间',
  `start_time` DATETIME NULL COMMENT 'ocr任务开始时间',
  `end_time` DATETIME NULL COMMENT 'ocr任务结束时间',
  `isdelete` TINYINT NOT NULL DEFAULT 1 COMMENT '0：记录删除，1：记录未删除；默认为1',
  PRIMARY KEY (`ocr_mission_id`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='OCR 任务表';

CREATE TABLE `processed_document` (
  `processed_doc_id` CHAR(36) NOT NULL COMMENT '主键，ocr处理后文档的唯一 ID（UUID）',
  `file_id` CHAR(36) NOT NULL COMMENT '源文件id',
  `file_name` VARCHAR(255) NOT NULL COMMENT '源文件名',
  `file_path` VARCHAR(1024) NOT NULL COMMENT '源文件存储路径',
  `type` TINYINT NOT NULL COMMENT '文档的内容类型（0=MARKDOWN文本；1=PNG图片）',
  `create_time` DATETIME NULL COMMENT '创建时间',
  `isdelete` TINYINT NOT NULL DEFAULT 1 COMMENT '0：记录删除，1：记录未删除；默认为1',
  PRIMARY KEY (`processed_doc_id`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='OCR 处理后文档表';

CREATE TABLE `gen_file_extract` (
  `extract_mission_id` CHAR(36) NOT NULL COMMENT '主键，结构抽取任务唯一 ID（UUID）',
  `file_id` CHAR(36) NOT NULL COMMENT '源文件id',
  `processed_doc_id` CHAR(36) NULL COMMENT '使用的 ProcessedDocument ID',
  `status_type` TINYINT NOT NULL COMMENT '任务状态（0=创建成功；1=运行中；2=任务失败；3=任务成功；4=等待中）',
  `file_node_id` CHAR(36) NULL COMMENT '成功后生成的 FileNode 在 Neo4j 中的 elementId',
  `error_message` VARCHAR(1024) NULL COMMENT '结构提取失败的原因说明',
  `create_time` DATETIME NULL COMMENT '结构提取任务创建时间',
  `start_time` DATETIME NULL COMMENT '结构提取任务开始时间',
  `end_time` DATETIME NULL COMMENT '结构提取任务结束时间',
  `isdelete` TINYINT NOT NULL DEFAULT 1 COMMENT '0：记录删除，1：记录未删除；默认为1',
  PRIMARY KEY (`extract_mission_id`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='结构抽取任务表';

CREATE TABLE `gen_file_embed` (
  `embed_mission_id` CHAR(36) NOT NULL COMMENT '主键，向量化任务唯一 ID（UUID）',
  `file_id` CHAR(36) NOT NULL COMMENT '源文件id',
  `file_node_id` CHAR(36) NULL COMMENT '使用的 FileNode elementId（来自结构化抽取）',
  `status_type` TINYINT NOT NULL COMMENT '任务状态（0=创建成功；1=运行中；2=任务失败；3=任务成功；4=等待中）',
  `error_message` VARCHAR(1024) NULL COMMENT '向量化任务失败的原因说明',
  `create_time` DATETIME NULL COMMENT '向量化任务创建时间',
  `start_time` DATETIME NULL COMMENT '向量化任务开始时间',
  `end_time` DATETIME NULL COMMENT '向量化任务结束时间',
  `isdelete` TINYINT NOT NULL DEFAULT 1 COMMENT '0：记录删除，1：记录未删除；默认为1',
  PRIMARY KEY (`embed_mission_id`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='文件向量化任务表';

CREATE TABLE `knowledge_document` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `document_id` CHAR(36) NOT NULL COMMENT '向量化文件 id',
  `document_name` VARCHAR(255) NULL COMMENT '向量化文件名称',
  `embed_id` CHAR(36) NOT NULL COMMENT '所属向量化任务 id',
  `type` INT NOT NULL COMMENT '分割类型：0=200字节、1=400字节、2=600字节、3=文本结构',
  `isdelete` INT NOT NULL DEFAULT 1 COMMENT '0：记录删除，1：记录未删除；默认为1',
  `create_time` DATETIME NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='知识文档分割结果表';

CREATE TABLE `collection_document` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `kb_id` CHAR(36) NOT NULL COMMENT '知识库 id',
  `document_id` CHAR(36) NOT NULL COMMENT '向量化文件 id',
  `isdelete` INT NOT NULL DEFAULT 1 COMMENT '0：记录删除，1：记录未删除；默认为1',
  `create_time` DATETIME NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='知识库与文档关联表';
