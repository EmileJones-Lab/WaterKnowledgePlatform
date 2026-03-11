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

    /**
     * 根据主键 ID 列表批量查询数据。
     * 返回结果的顺序通常应与输入的主键列表顺序保持一致。
     * 如果某个主键对应的数据不存在，则结果列表中该位置的值为 null。
     * @param keyList 要查询的主键 ID 列表
     * @return 包含对应数据实体的列表，缺失项为 null
     */
    fun findBatch(keyList: List<K>): List<V?>
}
