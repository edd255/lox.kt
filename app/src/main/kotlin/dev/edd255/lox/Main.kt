package dev.edd255.lox

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val lox = Lox()
        when (args.size) {
            0 -> lox.runPrompt()
            1 -> lox.runFile(args[0])
            else -> {
                println("Usage: lox [script]")
                exitProcess(64)
            }
        }
    } catch (exit: LoxExit) {
        exitProcess(exit.code)
    }
}
