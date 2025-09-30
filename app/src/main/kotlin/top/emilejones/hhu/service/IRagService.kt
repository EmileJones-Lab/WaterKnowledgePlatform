package top.emilejones.hhu.service

import java.nio.file.Path


interface IRagService {
    suspend fun saveFileToAllDatabase(filePath: Path)
}