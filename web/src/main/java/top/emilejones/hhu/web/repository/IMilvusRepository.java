package top.emilejones.hhu.web.repository;


import top.emilejones.hhu.web.entity.DenseRecallResult;

import java.util.List;

/**
 * 请求Milvus数据库的接口规范
 *
 * @author EmileJones
 */
public interface IMilvusRepository {
    /**
     * 根据向量去查找最相近的结果
     *
     * @param queryVector 查询向量
     * @param topK        需要查找的结果数量
     * @return 最相似结果，返回的对象中只有text属性和elementId属性
     */
    List<DenseRecallResult> search(List<Float> queryVector, int topK);
}
