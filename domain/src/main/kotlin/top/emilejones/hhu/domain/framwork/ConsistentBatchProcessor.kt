package top.emilejones.hhu.domain.framwork

/**
 * 通用的批量数据处理器接口
 *
 * @param K 主键类型
 * @param V 数据实体类型
 */
interface ConsistentBatchProcessor<K, V> : ConsistentDataProcessor<K, V> {

    /**
     * 批量保存数据。
     * 针对列表中的每个数据实体：如果不存在则新增，如果存在则修改。
     * @param valueList 要批量保存的数据实体列表
     */
    fun saveBatch(valueList: List<V>)

    /**
     * 根据主键 ID 列表批量删除数据
     * @param keyList 要删除的主键 ID 列表
     */
    fun deleteBatch(keyList: List<K>)
}
