package top.emilejones.hhu.textsplitter.service.strategy

import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode

interface RecallStrategy {
    fun exec(rawData: List<Pair<Neo4jFileNode, Neo4jTextNode>>): List<Pair<Neo4jFileNode, Neo4jTextNode>>
}
