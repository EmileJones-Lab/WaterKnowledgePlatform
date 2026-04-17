package top.emilejones.hhu.common

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * 对象对比工具类，用于提取两个对象之间的属性差异。
 * 
 * @author EmileJones
 */
object DiffUtils {
    /**
     * 对比当前对象与 [target] 对象，返回 [target] 中与当前对象不同的可写属性。
     * 
     * 常用于数据库更新操作，获取需要变更的字段。
     * 
     * @param source 源对象（如数据库中的旧对象）
     * @param target 目标对象（如需要更新的新对象）
     * @return 包含 target 中所有不同于 source 的属性 Map
     */
    fun <T : Any> diff(source: T, target: T): Map<String, Any?> {
        val diffMap = mutableMapOf<String, Any?>()
        val clazz = source::class

        clazz.memberProperties.forEach { prop ->
            @Suppress("UNCHECKED_CAST")
            val p = prop as kotlin.reflect.KProperty1<Any, *>
            p.isAccessible = true
            val sourceValue = p.get(source)
            val targetValue = p.get(target)

            if (sourceValue != targetValue) {
                diffMap[prop.name] = targetValue
            }
        }

        return diffMap
    }
}

/**
 * 扩展函数：对比当前对象（旧值）与新对象，返回新对象中的差异属性。
 */
fun <T : Any> T.diff(other: T): Map<String, Any?> {
    return DiffUtils.diff(this, other)
}
