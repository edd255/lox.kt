package dev.edd255.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.StandardCharsets

class Lox {
    private val errorReporter = ErrorReporter()
    private val interpreter = Interpreter(errorReporter)

    fun runFile(path: String): ExecutionResult {
        val source = Files.readString(Paths.get(path), StandardCharsets.UTF_8)
        return run(source)
    }

    fun runPrompt(showBanner: Boolean = true) {
        val input = InputStreamReader(System.`in`, StandardCharsets.UTF_8)
        val reader = BufferedReader(input)
        if (showBanner) {
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
        }
        while (true) {
            print(">>> ")
            val line = reader.readLine() ?: break
            run(line)
        }
    }

    fun runSource(source: String): ExecutionResult = run(source)

    private fun run(source: String): ExecutionResult {
        errorReporter.reset()
        val scanner = Scanner(source, errorReporter)
        val tokens: List<Token> = scanner.scanTokens()
        if (errorReporter.hadError) return currentResult()
        val parser = Parser(tokens, errorReporter)
        val statements = parser.parse()
        if (errorReporter.hadError) return currentResult()
        val resolver = Resolver(interpreter, errorReporter)
        resolver.resolve(statements)
        if (errorReporter.hadError) return currentResult()
        interpreter.interpret(statements)
        return currentResult()
    }

    private fun currentResult(): ExecutionResult =
        ExecutionResult(errorReporter.hadError, errorReporter.hadRuntimeError)
}
