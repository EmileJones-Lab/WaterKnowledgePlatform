package top.emilejones.hhu.service.strategy

import top.emilejones.hhu.enums.TextType
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jTextNode
import top.emilejones.hhu.repository.INeo4jRepository

class ObtainWholeTableStrategy(private val neo4jRepository: INeo4jRepository) : RecallStrategy {
    override fun exec(rawData: List<Pair<Neo4jFileNode, Neo4jTextNode>>): List<Pair<Neo4jFileNode, Neo4jTextNode>> {
        val resultSet: MutableSet<Pair<Neo4jFileNode, Neo4jTextNode>> = HashSet()
        for (datum in rawData) {
            resultSet.add(datum)
            // 如果不是表格则跳过上下查找
            if (TextType.TABLE != datum.second.type) continue
            // 向下找
            var nowPair: Pair<Neo4jFileNode, Neo4jTextNode>? = datum
            while (nowPair != null && TextType.TABLE == nowPair.second.type) {
                resultSet.add(nowPair)
                nowPair = neo4jRepository.nextNode(nowPair.second.elementId!!)
                if (resultSet.contains(nowPair)) break
            }
            // 向上找
            nowPair = datum
            while (TextType.TABLE == nowPair!!.second.type) {
                resultSet.add(nowPair)
                nowPair = neo4jRepository.preNode(nowPair.second.elementId!!)
                if (resultSet.contains(nowPair)) break
            }
        }
        return resultSet.stream().toList()
    }
}
