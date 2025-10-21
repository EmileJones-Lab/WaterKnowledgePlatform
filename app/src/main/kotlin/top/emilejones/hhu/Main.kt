package top.emilejones.hhu

import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import top.emilejones.hhu.command.Application
import top.emilejones.hhu.command.ClearDatabasesCommand
import top.emilejones.hhu.command.InsertCommand


suspend fun main(args: Array<String>) = Application()
    .subcommands(InsertCommand(), ClearDatabasesCommand())
    .main(args)