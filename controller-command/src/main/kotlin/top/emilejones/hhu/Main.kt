package top.emilejones.hhu

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import top.emilejones.hhu.command.Application
import top.emilejones.hhu.command.ConvertCommand
import top.emilejones.hhu.command.RetrieveCommand

@SpringBootApplication
class RagCommandApplication(
    private val application: Application,
    private val retrieveCommand: RetrieveCommand,
    private val convertCommand: ConvertCommand
) : CommandLineRunner {

    override fun run(vararg args: String) {
        application.subcommands(
            retrieveCommand,
            convertCommand
        ).main(args)
    }
}

fun main(args: Array<String>) {
    SpringApplicationBuilder(RagCommandApplication::class.java)
        .run(*args)
}
