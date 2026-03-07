package top.emilejones.hhu.domain.framwork

/**
 * 通用的数据处理器接口
 *
 * @param K 主键类型
 * @param V 数据实体类型
 */
interface ConsistentDataProcessor<K, V> {

    /**
     * 根据主键 ID 查询单个数据
     * @param key 主键 ID
     * @return 数据实体，如果不存在则返回 null
     */
    fun find(key: K): V?

    /**
     * 保存数据。
     * 如果数据在持久层中不存在，则执行新增逻辑；如果已存在，则执行修改逻辑。
     * @param value 要保存的数据实体
     */
    fun save(value: V)

    /**
     * 根据主键 ID 删除数据
     * @param key 主键 ID
     */
    fun delete(key: K)
}
