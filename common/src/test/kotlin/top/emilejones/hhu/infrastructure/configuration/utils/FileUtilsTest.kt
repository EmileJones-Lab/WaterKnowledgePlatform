package top.emilejones.hhu.infrastructure.configuration.utils


import top.emilejones.hhu.common.FileUtils
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileUtilsTest {

    @Test
    fun isPdf_ShouldReturnTrue_WhenFileCheckPdf() {
        val bytes = javaClass.getResourceAsStream("/testFile/test.pdf")?.readAllBytes()
            ?: throw RuntimeException("Resource /testFile/test.pdf not found")
        val result = FileUtils.checkPdf(bytes)
        assertTrue(result, "Expected test.pdf to be identified as PDF")
    }

    @Test
    fun isPdf_ShouldReturnFalse_WhenFileCheckNotPdf() {
        val bytes = javaClass.getResourceAsStream("/testFile/调度原则.docx")?.readAllBytes()
            ?: throw RuntimeException("Resource /testFile/调度原则.docx not found")
        val result = FileUtils.checkPdf(bytes)
        assertFalse(result, "Expected 调度原则.docx to NOT be identified as PDF")
    }
}
