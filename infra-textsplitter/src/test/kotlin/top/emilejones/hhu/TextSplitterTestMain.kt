package top.emilejones.hhu

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["top.emilejones.hhu"])
class TextSplitterTestMain

fun main(args: Array<String>) {
    runApplication<TextSplitterTestMain>(*args)
}
