package top.emilejones.hhu.repository.impl.neo4j.delegates

import top.emilejones.hhu.domain.dto.TextNode
import java.util.*
import kotlin.reflect.KProperty

/**
 * 用来绑定每一个TextNode和FileNode对应的elementId
 */
private class TextNodeDelegate {
    private val cache = WeakHashMap<TextNode, String?>()

    operator fun getValue(thisRef: TextNode, property: KProperty<*>): String? {
        return cache.getOrPut(thisRef) { null }
    }

    operator fun setValue(thisRef: TextNode, property: KProperty<*>, value: String?) {
        cache[thisRef] = value
    }
}

var TextNode.elementId: String? by TextNodeDelegate()