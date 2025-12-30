package top.emilejones.hhu.qa.dao.impl

import top.emilejones.hhu.qa.dao.IQARepository
import top.emilejones.hhu.qa.entity.QAPair
import java.io.File
import java.nio.file.Files

class QASingleFileRepository(private val file: File) : IQARepository {
    init {
        if (!file.exists()) {
            Files.createDirectories(file.parentFile.toPath())
            file.createNewFile()
        }
    }

    override fun batchSave(qaList: List<QAPair>): Int {
        val data = qaList.joinToString(separator = "\n", postfix = "\n") { "\"${it.question}\",\"${it.answer}\"" }
        file.appendText(data)
        return qaList.size
    }

}