package top.emilejones.hhu.service

import org.slf4j.LoggerFactory
import top.emilejones.hhu.parser.MarkdownStructureParser
import top.emilejones.hhu.repository.neo4j.INeo4jRepository
import java.io.File

class Neo4jService(
    private val repository: INeo4jRepository
) : AutoCloseable {

    private val logger = LoggerFactory.getLogger(Neo4jService::class.java)

    fun save(file: File) {
        val fileNode = repository.searchFileNodeByFileName(file.name)
        if (fileNode != null){
            logger.debug("The file [{}] is already exist!", file.name)
            return
        }
        
        logger.debug("Start read file [{}] as a tree structure", file.name)
        val parser = MarkdownStructureParser(file)
        val result = parser.run()
        logger.debug("Success read file [{}] as a tree structure", file.name)
        repository.insertTree(result)
        logger.debug("Success save tree structure of file [{}] in neo4j", file.name)
    }

    fun batchSaveInDir(dir: File) {
        dir.walk().forEach {
            logger.info("Visit file [{}]", it.name)
            if (it.isDirectory) return
            save(it)
        }
    }

    override fun close() {
        repository.close()
    }
}