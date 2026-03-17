package top.emilejones.hhu.qa

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import top.emilejones.hhu.qa.converter.impl.ConcurrentDifyApiConverter
import top.emilejones.hhu.qa.dao.impl.QASingleFileRepository
import java.io.File

fun main(): Unit = runBlocking {
    val url = "http://10.196.83.122:8103/v1/workflows/run"
    val apiKey = "app-0odhhjqifZpvQ7g7pTUvdJNd"
    val difyConverter = ConcurrentDifyApiConverter(url, apiKey)
    val repository = QASingleFileRepository(File("./qa.csv"))

    val test = """
        淮河水资源调度考核断面 37 个，分别为：王家坝、蚌埠、小柳巷、洪泽湖（蒋坝）、界首、沈丘、颍上、周口、叶集、蒋家集、陈村、班台、付桥闸、安溜、蒙城、黄口集闸、耿庄闸、固镇闸、永城闸、宿县闸、团结闸、伊桥、枯河闸、泗洪（濉）、泗洪（老）、飞沙河水库、南界漫水坝、马家畈、平桥、魏家冲、竹竿铺（三）、罗山、明光（二）、旧县闸、天长（白）、高邮湖控制断面、高邮（高）。考核断面基本信息见表 2。
    """.trimIndent()
    val filePath = "/Users/sunhongfei/Downloads/淮河水资源调度方案（非最终稿）/淮河水资源调度方案-v4.md"
    val qaList = File(filePath).readText().lines()
        .map {
            async {
                difyConverter.convert(it)
            }
        }.awaitAll()
        .mapNotNull {
            it.getOrNull()
        }.flatten()
//    val qaList = difyConverter.convert(test).getOrDefault(emptyList())
    repository.batchSave(qaList)
}