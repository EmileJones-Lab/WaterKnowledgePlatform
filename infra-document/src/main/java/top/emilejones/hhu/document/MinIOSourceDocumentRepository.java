package top.emilejones.hhu.document;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import top.emilejones.hhu.document.entity.SourceDocumentPO;
import top.emilejones.hhu.document.mapper.SourceDocumentMapper;
import top.emilejones.hhu.domain.document.SourceDocument;
import top.emilejones.hhu.domain.document.SourceFileType;
import top.emilejones.hhu.domain.document.infrastruction.SourceDocumentRepository;
import org.springframework.stereotype.Repository;
import top.emilejones.hhu.env.pojo.MinioConfig;

import java.io.InputStream;
import java.util.Optional;


/**
 * MinIOSourceDocumentRepository
 * 基于 MySQL + MinIO 的 SourceDocumentRepository 实现。
 * 功能说明：
 * 1. 通过 SourceDocumentMapper 从 MySQL 查询文件元数据（文件名、类型、MinIO 路径等）。
 * 2. 通过 MinioClient 根据文件路径从 MinIO 读取文件内容并返回 InputStream。
 * MinIO 路径格式：
 *      http://{host}:{port}/{bucket}/{objectKey}
 * 示例：
 *      http://10.196.83.122:9000/bamboo/2024/11/27/xxx.docx
 * 其中：
 *      bucket     → 从路径中解析或由配置提供
 *      objectKey  → 示例中的 2024/11/27/xxx.docx
 * 本类通过构造函数注入：
 *      - MinioConfig          MinIO 基础配置
 *      - MinioClient         用于访问 MinIO 的 SDK 客户端
 *      - SourceDocumentMapper   MySQL 访问接口
 * 该类属于基础设施层，用于为领域层提供文件元数据与文件内容读取能力。
 * @author Yeyezhi
 */
@Repository
public class MinIOSourceDocumentRepository implements SourceDocumentRepository {

    private final MinioConfig minioConfig;
    private final MinioClient minioClient;
    private final SourceDocumentMapper sourceDocumentMapper;

    @Autowired
    public MinIOSourceDocumentRepository(
            MinioConfig minioConfig,
            MinioClient minioClient,
            SourceDocumentMapper sourceDocumentMapper
    ) {
        this.minioConfig = minioConfig;
        this.minioClient = minioClient;
        this.sourceDocumentMapper = sourceDocumentMapper;
    }

    /**
     * 根据相对路径从 MinIO 读取文件内容。
     * path 格式： /{bucket}/{objectKey}
     *
     * 示例：/bamboo/2024/11/27/xxx.docx
     * @return InputStream 文件内容的输入流，可用于后续读取解析
     */
    @NotNull
    @Override
    public InputStream openContent(@NotNull String path) {
        try {
            if (!path.startsWith("/")) {
                throw new IllegalArgumentException("MinIO 文件路径必须以 '/' 开头: " + path);
            }

            // 去掉开头的 '/'
            String clean = path.substring(1);  // bamboo/2024/11/27/xxx.docx

            // 提取 bucket 与 objectKey
            int slashIndex = clean.indexOf("/");
            if (slashIndex < 0) {
                throw new IllegalArgumentException("无效的 MinIO 文件路径（缺少 objectKey）: " + path);
            }

            String bucket = clean.substring(0, slashIndex);
            String objectKey = clean.substring(slashIndex + 1);

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("从 MinIO 读取文件失败，path=" + path, e);
        }
    }

    /**
     * 根据文件 ID 从 MySQL 查询文件元数据，并封装为领域对象 SourceDocument。
     * 查询结果包含：
     *      - 文件名
     *      - 目录的路径（catapath）
     *      - MinIO 文件存储路径
     *      - 文件类型（pdf/doc/docx/txt/md）
     * @param id 文件在数据库中的主键 ID
     * @return Optional<SourceDocument> 若存在记录则返回对应领域对象，否则返回 Optional.empty()
     */
    @NotNull
    @Override
    public Optional<SourceDocument> findSourceDocumentById(@NotNull String id) {
        SourceDocumentPO sourceDocumentPO = sourceDocumentMapper.findById(id);
        if (sourceDocumentPO == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(sourceDocumentPO));
    }

    /**
     * 将SourceDocumentPO封装成SourceDocument
     */
    private SourceDocument toDomain(SourceDocumentPO sourceDocumentPO) {
        return new SourceDocument(
                String.valueOf(sourceDocumentPO.getId()),
                sourceDocumentPO.getFilename(),
                sourceDocumentPO.getCatapath(),
                sourceDocumentPO.getFilepath(),
                parseFileType(sourceDocumentPO.getFiletype())
        );
    }

    /**
     * 映射数据库 filetype → 枚举 SourceFileType
     */
    private SourceFileType parseFileType(String type) {
        if (type == null) return SourceFileType.TXT;
        return switch (type.toLowerCase()) {
            case "pdf" -> SourceFileType.PDF;
            case "doc" -> SourceFileType.DOC;
            case "docx" -> SourceFileType.DOCX;
            case "md", "markdown" -> SourceFileType.MARKDOWN;
            default -> SourceFileType.TXT;
        };
    }
}
