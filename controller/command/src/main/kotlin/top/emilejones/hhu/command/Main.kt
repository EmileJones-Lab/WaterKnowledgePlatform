package top.emilejones.hhu.command

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import top.emilejones.hhu.command.subcommand.ConvertCommand
import top.emilejones.hhu.command.subcommand.EmbedCommand
import top.emilejones.hhu.command.subcommand.ExtractCommand
import top.emilejones.hhu.command.subcommand.RetrieveCommand

@SpringBootApplication(scanBasePackages = ["top.emilejones.hhu"])
class RagCommandApplication(
    private val application: Application,
    private val retrieveCommand: RetrieveCommand,
    private val convertCommand: ConvertCommand,
    private val extractCommand: ExtractCommand,
    private val embedCommand: EmbedCommand
) : CommandLineRunner {

    override fun run(vararg args: String) {
        application.subcommands(
            retrieveCommand,
            convertCommand,
            extractCommand,
            embedCommand
        ).main(args)
    }
}

fun main(args: Array<String>) {
    SpringApplicationBuilder(RagCommandApplication::class.java)
        .run(*args)
}
