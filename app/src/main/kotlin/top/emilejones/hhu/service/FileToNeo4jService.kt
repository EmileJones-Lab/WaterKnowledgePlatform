package top.emilejones.hhu.service

import org.slf4j.LoggerFactory
import top.emilejones.hhu.parser.MarkdownStructureParser
import top.emilejones.hhu.repository.neo4j.INeo4jRepository
import java.io.File

class FileToNeo4jService(
    private val repository: INeo4jRepository
) : AutoCloseable {

    private val logger = LoggerFactory.getLogger(FileToNeo4jService::class.java)

    fun save(file: File) {
        logger.trace("Start read file [{}] as a tree structure", file.name)
        val parser = MarkdownStructureParser(file)
        val result = parser.run()
        logger.trace("Success read file [{}] as a tree structure", file.name)
        repository.insertTree(result)
        logger.trace("Success save tree structure of file [{}] in neo4j", file.name)
    }

    fun batchSaveInDir(dir: File) {
        dir.walk().forEach {
            logger.debug("Visit file [{}]", it.name)
            if (it.isDirectory) return
            save(it)
        }
    }

    override fun close() {
        repository.close()
    }
}