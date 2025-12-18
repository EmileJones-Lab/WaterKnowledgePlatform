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
    private final String bucketName = "bamboo"; // 建议从配置类读取

    public MinioFileStorageRepository(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String save(InputStream content, String objectName) {
        try {
            // 使用自定义的 MinioClient 配置进行保存
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(content, content.available(), -1)
                            .build()
            );
            // 返回符合约定的路径格式：/bucket/objectKey
            return "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("MinIO 保存失败: " + objectName, e);
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