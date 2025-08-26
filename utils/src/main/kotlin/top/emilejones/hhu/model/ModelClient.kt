package top.emilejones.hhu.model

interface ModelClient {
    fun embedding(text:String): List<Float>
}