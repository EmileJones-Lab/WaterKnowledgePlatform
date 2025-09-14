package top.emilejones.hhu

import kotlinx.coroutines.runBlocking
import top.emilejones.hhu.model.impl.XinferenceHttpClient
import top.emilejones.hhu.repository.milvus.impl.MilvusRepositoryImpl
import top.emilejones.hhu.repository.neo4j.impl.Neo4jRepositoryImpl
import top.emilejones.hhu.service.Neo4jService
import top.emilejones.hhu.service.MilvusService
import top.emilejones.huu.env.MilvusEnvironment
import top.emilejones.huu.env.Neo4jEnvironment
import top.emilejones.huu.env.XinferenceEnvironment
import java.io.File

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
    port = Neo4jEnvironment.PORT,
    databaseName = Neo4jEnvironment.DATABASE
)
val modelClient = XinferenceHttpClient(XinferenceEnvironment.HOST, XinferenceEnvironment.PORT)
val fileToNeo4jService = Neo4jService(neo4jRepository)
val neo4jToMilvusService = MilvusService(milvusRepository, neo4jRepository, modelClient)

fun main() = runBlocking {

    File("/Users/sunhongfei/Downloads/知识问答示例相关文件").walk().forEach {
        if (it.isDirectory)
            return@forEach
        if (it.name.split('.').last().lowercase() != "md")
            return@forEach

        saveFileToDB(it)
    }

    fileToNeo4jService.close()
    neo4jToMilvusService.close()
}

private suspend fun saveFileToDB(sourceFile: File) {
    fileToNeo4jService.save(sourceFile)
    neo4jToMilvusService.saveByFilenameFromNeo4j(sourceFile.name)
}