package top.emilejones.hhu

import kotlinx.coroutines.runBlocking
import top.emilejones.hhu.model.impl.XinferenceHttpClient
import top.emilejones.hhu.repository.milvus.impl.MilvusRepositoryImpl
import top.emilejones.hhu.repository.neo4j.impl.Neo4jRepositoryImpl
import top.emilejones.hhu.service.FileToNeo4jService
import top.emilejones.hhu.service.Neo4jToMilvusService
import top.emilejones.huu.env.MilvusEnvironment
import top.emilejones.huu.env.Neo4jEnvironment
import top.emilejones.huu.env.XinferenceEnvironment
import java.io.File

fun main() = runBlocking {
    val filePath = "/Users/sunhongfei/Downloads/测试文档/淮河水资源调度方案（非最终稿）/淮河水资源调度方案.md"
    val fileName = "淮河水资源调度方案.md"

    val milvusRepository = MilvusRepositoryImpl(
        port = MilvusEnvironment.PORT,
        host = MilvusEnvironment.HOST,
        databaseName = MilvusEnvironment.DATABASE_NAME,
        collectionName = MilvusEnvironment.COLLECTION_NAME
    )
    val neo4jRepository = Neo4jRepositoryImpl(
        username = Neo4jEnvironment.USER,
        password = Neo4jEnvironment.PASSWORD,
        host = Neo4jEnvironment.HOST,
        port = Neo4jEnvironment.PORT
    )
    val modelClient = XinferenceHttpClient(XinferenceEnvironment.HOST, XinferenceEnvironment.PORT)

    val fileToNeo4jService = FileToNeo4jService(neo4jRepository)
    val neo4jToMilvusService = Neo4jToMilvusService(milvusRepository, neo4jRepository, modelClient)

    fileToNeo4jService.save(File(filePath))
    neo4jToMilvusService.saveByFilenameFromNeo4j(fileName)

    fileToNeo4jService.close()
    neo4jToMilvusService.close()
}