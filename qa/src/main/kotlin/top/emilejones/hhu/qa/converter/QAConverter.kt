package top.emilejones.hhu.qa.converter

import top.emilejones.hhu.qa.entity.QAPair

interface QAConverter {
    suspend fun convert(text: String): Result<List<QAPair>>
}