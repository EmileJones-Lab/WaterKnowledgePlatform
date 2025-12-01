package top.emilejones.hhu.qa.dao

import top.emilejones.hhu.qa.entity.QAPair

interface IQARepository {
    fun batchSave(qaList: List<QAPair>): Int
}