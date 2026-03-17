package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.*
import top.emilejones.hhu.textsplitter.service.IRecallService
import java.util.*

@Service
class Neo4jNodeRepositoryAdaptor(
    private val neo4jRepository: INeo4jRepository,
    private val recallService: IRecallService
) : NodeRepository {
    override fun findFileNodeByFileNodeId(fileNodeId: String): Optional<FileNode> {
        return Optional.ofNullable(neo4jRepository.searchNeo4jFileNodeByNodeId(fileNodeId)?.asFileNode())
    }

    override fun findTextNodeListByFileNodeId(fileNodeId: String): List<TextNode> {
        val fileNode = neo4jRepository.searchNeo4jFileNodeByNodeId(fileNodeId)
        require(fileNode != null) { "不存在FileNode" }
        val neo4jTextNodeList = neo4jRepository.searchNeo4jTextNodeByFileId(fileNode.fileId)
        return neo4jTextNodeList.map { it.asTextNode(fileNode) }.toList()
    }

    override fun findTextNodeByTextNodeId(textNodeId: String): Optional<TextNode> {
        val neo4jTextNode = neo4jRepository.searchNeo4jTextNodeByNodeId(textNodeId) ?: return Optional.empty()
        val neo4jFileNode = neo4jRepository.searchNeo4jFileNodeByTextNode(textNodeId)
        return Optional.ofNullable(neo4jTextNode.asTextNode(neo4jFileNode))
    }

    override fun saveTextNode(textNode: TextNode) {
        val dbDatum = neo4jRepository.searchNeo4jTextNodeByNodeId(textNode.id)
        val newDatum = textNode.toNeo4jTextNode()
        if (dbDatum == null) {
            neo4jRepository.insertNeo4jTextNode(newDatum)
            return
        }
        neo4jRepository.updateNodeByElementId(dbDatum.elementId!!, dbDatum.diff(newDatum))
    }

    override fun recallTextNode(query: String, collectionName: String): List<TextNode> {
        return recallService.recallNode(query, collectionName)
            .map {
                val fileNode = neo4jRepository.searchNeo4jFileNodeByTextNode(it.id)
                it.asTextNode(fileNode)
            }.toList()
    }

    override fun deleteAllNodeByFileNodeId(id: String) {
        val fileNode = neo4jRepository.searchNeo4jFileNodeByNodeId(id) ?: return
        neo4jRepository.searchNeo4jTextNodeByFileId(fileNode.fileId).forEach {
            neo4jRepository.deleteTextNodeById(it.id)
        }
        neo4jRepository.deleteFileNodeById(fileNode.id)
    }

    override fun saveFileNode(fileNode: FileNode) {
        val dbDatum = neo4jRepository.searchNeo4jFileNodeByNodeId(fileNode.id)
        val newDatum = fileNode.toNeo4jFileNode()
        if (dbDatum == null) {
            neo4jRepository.insertNeo4jFileNode(newDatum)
            return
        }
        neo4jRepository.updateNodeByElementId(dbDatum.elementId!!, dbDatum.diff(newDatum))
    }
}
