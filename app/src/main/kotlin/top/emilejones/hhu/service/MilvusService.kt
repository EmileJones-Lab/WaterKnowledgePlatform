package top.emilejones.hhu.service

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.repository.milvus.IMilvusRepository
import top.emilejones.hhu.repository.milvus.po.EmbeddingDatum
import top.emilejones.hhu.repository.neo4j.INeo4jRepository

class MilvusService(
    private val milvusRepository: IMilvusRepository,
    private val neo4jRepository: INeo4jRepository,
    private val modelClient: ModelClient
) : AutoCloseable {

    private val logger = LoggerFactory.getLogger(MilvusService::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun saveByFilenameFromNeo4j(filename: String) {
        logger.debug("Start find all nodes about file [{}]", filename)
        val textNodeList = neo4jRepository.searchNeo4jTextNodeByFilename(filename)
        logger.debug("Search nodes about file [{}] success! The nodes number is [{}]", filename, textNodeList.size)
        logger.debug("Start embed file [{}]", filename)
        val embeddingData = textNodeList.map {
            scope.async {
                EmbeddingDatum(
                    vector = modelClient.embedding(it.text),
                    text = it.text,
                    neo4jElementId = it.elementId!!,
                    type = it.type
                )
            }
        }.awaitAll()
        logger.debug("Embedding file [{}] is finish!", filename)
        logger.debug("Start save file [{}] in Milvus", filename)
        milvusRepository.batchInsert(embeddingData)
        logger.debug("Success save all nodes about file [{}] in milvus", filename)
    }

    override fun close() {
        milvusRepository.close()
        neo4jRepository.close()
    }
}