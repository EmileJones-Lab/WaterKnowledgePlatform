package top.emilejones.hhu.textsplitter.repository

import top.emilejones.hhu.textsplitter.repository.neo4j.INeo4jCommandRepository
import top.emilejones.hhu.textsplitter.repository.neo4j.INeo4jQueryRepository
import top.emilejones.hhu.textsplitter.repository.neo4j.INeo4jTreeRepository

/**
 * Neo4j 综合仓库接口
 * 聚合了命令、查询和树形结构操作接口
 *
 * @author EmileJones
 */
interface INeo4jRepository : INeo4jCommandRepository, INeo4jQueryRepository, INeo4jTreeRepository