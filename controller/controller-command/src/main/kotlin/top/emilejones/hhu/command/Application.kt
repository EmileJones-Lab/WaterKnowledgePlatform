package top.emilejones.hhu.command

import com.github.ajalt.clikt.core.CliktCommand
import org.springframework.stereotype.Component

/**
 * 命令行工具运行方式
 * @author EmileJones
 */
@Component
class Application : CliktCommand(name = "rag-command") {

    override fun run() = Unit
}