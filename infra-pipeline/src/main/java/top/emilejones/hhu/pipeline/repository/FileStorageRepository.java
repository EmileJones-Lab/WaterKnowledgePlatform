package top.emilejones.hhu.pipeline.repository;

import java.io.InputStream;

/**
 * 这是一个通用的文件存储接口
 * 就像 Mapper 接口一样，Service 只调用它，不关心它是存在 MinIO 还是别的地方
 * @author Yeyezhi
 */
public interface FileStorageRepository {

    // 保存并返回最终在存储系统里的路径/Key
    String save(InputStream content, String objectName);

    // 打开存储系统里的流
    InputStream open(String path);
}