package top.emilejones.hhu.service.strategy

import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jTextNode

interface RecallStrategy {
    fun exec(rawData: List<Pair<Neo4jFileNode, Neo4jTextNode>>): List<Pair<Neo4jFileNode, Neo4jTextNode>>
}
