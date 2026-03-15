package top.emilejones.hhu.pipeline.repository.Impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Repository;
import top.emilejones.hhu.pipeline.repository.FileStorageRepository;

import java.io.InputStream;

/**
 * 实现文件存储到minio接口
 * @author Yeyezhi
 */
@Repository // 标记为持久层组件
public class MinioFileStorageRepository implements FileStorageRepository {

    private final MinioClient minioClient;

    public MinioFileStorageRepository(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String save(InputStream content, String path) {
        try {
            // 解析路径以获取 bucketName 和 objectKey
            // 输入格式如：/bamboo/OCR/2025/11/sdf.md
            String clean = path.startsWith("/") ? path.substring(1) : path;
            int slashIndex = clean.indexOf("/");
            if (slashIndex == -1) {
                throw new IllegalArgumentException("路径格式错误，必须包含 bucketName: " + path);
            }
            String bucketName = clean.substring(0, slashIndex);
            String objectKey = clean.substring(slashIndex + 1);

            // 使用解析出的 bucketName 和 objectKey 进行保存
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(content, content.available(), -1)
                            .build()
            );
            // 返回原始路径格式 /bucket/objectKey
            return path.startsWith("/") ? path : "/" + path;
        } catch (Exception e) {
            throw new RuntimeException("MinIO 保存失败: " + path, e);
        }
    }

    @Override
    public InputStream open(String path) {
        try {
            // 解析路径（复用你之前的逻辑）
            String clean = path.startsWith("/") ? path.substring(1) : path;
            int slashIndex = clean.indexOf("/");
            String bucket = clean.substring(0, slashIndex);
            String objectKey = clean.substring(slashIndex + 1);

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO 读取失败: " + path, e);
        }
    }
}