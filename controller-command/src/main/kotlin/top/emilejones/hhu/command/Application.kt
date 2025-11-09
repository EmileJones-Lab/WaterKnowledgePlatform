package top.emilejones.hhu.command

import com.github.ajalt.clikt.command.SuspendingCliktCommand

/**
 * 命令行工具运行方式
 * @author EmileJones
 */
class Application : SuspendingCliktCommand(name = "insert") {

    override suspend fun run() = Unit
}