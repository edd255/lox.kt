package dev.edd255.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    private val errorReporter = ErrorReporter()
    private val interpreter = Interpreter(errorReporter)

    fun runFile(path: String) {
        val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (errorReporter.hadError) exitProcess(65)
        if (errorReporter.hadRuntimeError) exitProcess(70)
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
         print(
"""
 ▄▄▄▄▄▄▄ ▄▄   ▄▄ ▄▄▄▄▄▄▄    ▄▄▄     ▄▄▄▄▄▄▄ ▄▄   ▄▄    ▄▄▄ ▄▄    ▄ ▄▄▄▄▄▄▄ ▄▄▄▄▄▄▄ ▄▄▄▄▄▄   ▄▄▄▄▄▄▄ ▄▄▄▄▄▄   ▄▄▄▄▄▄▄ ▄▄▄▄▄▄▄ ▄▄▄▄▄▄▄ ▄▄▄▄▄▄
█       █  █ █  █       █  █   █   █       █  █▄█  █  █   █  █  █ █       █       █   ▄  █ █       █   ▄  █ █       █       █       █   ▄  █
█▄     ▄█  █▄█  █    ▄▄▄█  █   █   █   ▄   █       █  █   █   █▄█ █▄     ▄█    ▄▄▄█  █ █ █ █    ▄  █  █ █ █ █    ▄▄▄█▄     ▄█    ▄▄▄█  █ █ █
  █   █ █       █   █▄▄▄   █   █   █  █ █  █       █  █   █       █ █   █ █   █▄▄▄█   █▄▄█▄█   █▄█ █   █▄▄█▄█   █▄▄▄  █   █ █   █▄▄▄█   █▄▄█▄
  █   █ █   ▄   █    ▄▄▄█  █   █▄▄▄█  █▄█  ██     █   █   █  ▄    █ █   █ █    ▄▄▄█    ▄▄  █    ▄▄▄█    ▄▄  █    ▄▄▄█ █   █ █    ▄▄▄█    ▄▄  █
  █   █ █  █ █  █   █▄▄▄   █       █       █   ▄   █  █   █ █ █   █ █   █ █   █▄▄▄█   █  █ █   █   █   █  █ █   █▄▄▄  █   █ █   █▄▄▄█   █  █ █
  █▄▄▄█ █▄▄█ █▄▄█▄▄▄▄▄▄▄█  █▄▄▄▄▄▄▄█▄▄▄▄▄▄▄█▄▄█ █▄▄█  █▄▄▄█▄█  █▄▄█ █▄▄▄█ █▄▄▄▄▄▄▄█▄▄▄█  █▄█▄▄▄█   █▄▄▄█  █▄█▄▄▄▄▄▄▄█ █▄▄▄█ █▄▄▄▄▄▄▄█▄▄▄█  █▄█


"""
         )
        while (true) {
            print(">>> ")
            val line = reader.readLine() ?: break
            run(line)
        }
    }

    private fun run(source: String) {
        errorReporter.reset()
        val scanner = Scanner(source, errorReporter)
        val tokens: List<Token> = scanner.scanTokens()
        if (errorReporter.hadError) return
        val parser = Parser(tokens, errorReporter)
        if (errorReporter.hadError) return
        val statements = parser.parse()
        if (errorReporter.hadError) return
        val resolver = Resolver(interpreter, errorReporter)
        resolver.resolve(statements)
        if (errorReporter.hadError) return
        interpreter.interpret(statements)
    }
}
