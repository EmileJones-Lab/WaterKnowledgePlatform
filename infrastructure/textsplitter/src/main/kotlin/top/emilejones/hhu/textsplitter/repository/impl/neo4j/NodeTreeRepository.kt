package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.QueryRunner
import org.neo4j.driver.SessionConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jRelationship
import top.emilejones.hhu.textsplitter.domain.po.Neo4jRelationshipType
import top.emilejones.hhu.common.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.delegates.elementId
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.*

@Repository
class NodeTreeRepository(
    private val driver: Driver,
    private val neo4jConfig: Neo4jConfig
) {

    private val logger = LoggerFactory.getLogger(NodeTreeRepository::class.java)

    fun insertTree(rootNode: TextNodeDTO) {
        if (rootNode.fileNode == null)
            throw IllegalArgumentException("The fileNode of rootNode must be not null!")

        rootNode.getChild(0).preNode = null
        for (i in 0..<rootNode.childNum()) {
            rootNode.getChild(i).parentNode = null
        }

        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            session.beginTransaction().use { transaction ->
                logger.debug("Start insert tree transaction")
                val result = runCatching {
                    val neo4jFileNode = transaction.insertFileNode(rootNode.fileNode!!.toNeo4jFileNode())
                    for (i in 0..<rootNode.childNum()) {
                        deepVisitAndInsertNode(neo4jFileNode, rootNode.getChild(i), transaction)
                    }
                }
                result.fold(
                    onSuccess = {
                        transaction.commit()
                        logger.debug("Success insert [{}] Tree, transaction commited.", rootNode.fileNode!!.fileId)
                    },
                    onFailure = {
                        transaction.rollback()
                        logger.error("Taking an exception when insert ${rootNode.fileNode!!.fileId} Tree", it)
                        throw RuntimeException(it)
                    }
                )

            }
        }
    }

    private fun deepVisitAndInsertNode(
        fileNode: Neo4jFileNode,
        nowNode: TextNodeDTO,
        queryRunner: QueryRunner
    ) {
        val nowNeo4jTextNode = queryRunner.insertTextNode(nowNode.toNeo4jTextNode())
        nowNode.elementId = nowNeo4jTextNode.elementId
        insertAllRelationShip(fileNode, nowNode, queryRunner)

        for (i in 0..<nowNode.childNum()) {
            deepVisitAndInsertNode(fileNode, nowNode.getChild(i), queryRunner)
        }
    }

    private fun insertAllRelationShip(
        fileNode: Neo4jFileNode,
        nowNode: TextNodeDTO,
        queryRunner: QueryRunner
    ) {
        val nowNodeElementId = nowNode.elementId!!

        if (nowNode.parentNode != null) {
            // 孩子关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNode.parentNode!!.elementId!!,
                    endNodeElementId = nowNodeElementId,
                    type = Neo4jRelationshipType.CHILD
                )
            )

            // 父亲关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNodeElementId,
                    endNodeElementId = nowNode.parentNode!!.elementId!!,
                    type = Neo4jRelationshipType.PARENT
                )
            )
        }


        if (nowNode.preNode != null) {
            // 前序关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNodeElementId,
                    endNodeElementId = nowNode.preNode!!.elementId!!,
                    type = Neo4jRelationshipType.PRE_SEQUENCE
                )
            )

            // 后序关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNode.preNode!!.elementId!!,
                    endNodeElementId = nowNodeElementId,
                    type = Neo4jRelationshipType.NEXT_SEQUENCE
                )
            )
        }

        // 文件包含关系
        queryRunner.insertRelationship(
            Neo4jRelationship(
                startNodeElementId = fileNode.elementId!!,
                endNodeElementId = nowNodeElementId,
                type = Neo4jRelationshipType.CONTAIN
            )
        )
    }
}