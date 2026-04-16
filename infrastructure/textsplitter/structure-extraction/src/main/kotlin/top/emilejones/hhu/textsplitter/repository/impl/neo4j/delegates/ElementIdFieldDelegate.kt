package top.emilejones.hhu.textsplitter.repository.impl.neo4j.delegates

import top.emilejones.hhu.textsplitter.domain.dto.FileNodeDTO
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import java.util.*
import kotlin.reflect.KProperty

/**
 * 用来绑定每一个TextNode和FileNode对应的elementId
 */
private class TextNodeDelegate {
    private val cache = WeakHashMap<TextNodeDTO, String?>()

    operator fun getValue(thisRef: TextNodeDTO, property: KProperty<*>): String? {
        return cache.getOrPut(thisRef) { null }
    }

    operator fun setValue(thisRef: TextNodeDTO, property: KProperty<*>, value: String?) {
        cache[thisRef] = value
    }
}

var TextNodeDTO.elementId: String? by TextNodeDelegate()

private class FileNodeDelegate {
    private val cache = WeakHashMap<FileNodeDTO, String?>()

    operator fun getValue(thisRef: FileNodeDTO, property: KProperty<*>): String? {
        return cache.getOrPut(thisRef) { null }
    }

    operator fun setValue(thisRef: FileNodeDTO, property: KProperty<*>, value: String?) {
        cache[thisRef] = value
    }
}

var FileNodeDTO.elementId: String? by FileNodeDelegate()