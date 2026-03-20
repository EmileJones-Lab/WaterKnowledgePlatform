package top.emilejones.hhu.infrastructure.configuration.utils


import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FileUtilsTest {

    @Test
    fun isPdf_ShouldReturnTrue_WhenFileCheckPdf() {
        val inputStream: InputStream? = javaClass.getResourceAsStream("/testFile/test.pdf")
        if (inputStream != null) {
            val result = FileUtils.checkPdf(inputStream)
            assertNotNull(result, "Expected test.pdf to be identified as PDF")
        } else {
            throw RuntimeException("Resource /testFile/test.pdf not found")
        }
    }

    @Test
    fun isPdf_ShouldReturnFalse_WhenFileCheckNotPdf() {
        val inputStream: InputStream? = javaClass.getResourceAsStream("/testFile/调度原则.docx")
        if (inputStream != null) {
            val result = FileUtils.checkPdf(inputStream)
            assertNull(result, "Expected 调度原则.docx to NOT be identified as PDF")
        } else {
            throw RuntimeException("Resource /testFile/调度原则.docx not found")
        }
    }
}
