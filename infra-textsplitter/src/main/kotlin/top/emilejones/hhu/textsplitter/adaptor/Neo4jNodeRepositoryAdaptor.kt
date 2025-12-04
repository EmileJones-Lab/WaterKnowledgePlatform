package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.pipeline.FileNode
import top.emilejones.hhu.domain.pipeline.TextNode
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.NodeRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asFileNode
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asTextNode
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.diff
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.toNeo4jTextNode
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
        val neo4jTextNodeList = neo4jRepository.searchNeo4jTextNodeByFileId(fileNodeId)
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
}