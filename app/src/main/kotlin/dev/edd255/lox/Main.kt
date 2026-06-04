package dev.edd255.lox

import kotlin.system.exitProcess

private const val VERSION = "0.1.0"

fun main(args: Array<String>) {
    exitProcess(runCli(args))
}

fun runCli(args: Array<String>): Int {
    return try {
        val lox = Lox()
        when {
            args.isEmpty() -> {
                lox.runPrompt()
                0
            }
            args.size == 1 && args[0] in setOf("--help", "-h") -> {
                printHelp()
                0
            }
            args.size == 1 && args[0] == "--version" -> {
                println("lox.kt $VERSION")
                0
            }
            args.size == 1 && args[0] in setOf("--quiet", "--no-banner") -> {
                lox.runPrompt(showBanner = false)
                0
            }
            args.size == 1 && !args[0].startsWith("-") -> exitCode(lox.runFile(args[0]))
            else -> usageError()
        }
    } catch (exit: LoxExit) {
        exit.code
    }
}

private fun printHelp() {
    println(
        """
        Usage: lox [options] [script]

        Options:
          -h, --help       Show this help message.
          --version        Show the interpreter version.
          --quiet          Start the REPL without the banner.
          --no-banner      Alias for --quiet.

        With no script, lox starts an interactive prompt.
        """.trimIndent()
    )
}

private fun usageError(): Int {
    System.err.println("Usage: lox [options] [script]")
    return 64
}

private fun exitCode(result: ExecutionResult): Int =
    when {
        result.hadCompileError -> 65
        result.hadRuntimeError -> 70
        else -> 0
    }
