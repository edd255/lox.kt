package dev.edd255.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    private val interpreter = Interpreter()

    fun runFile(path: String) {
        val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (ErrorReporter.hadError) {
            exitProcess(65)
        }
        if (ErrorReporter.hadRuntimeError) {
            exitProcess(70)
        }
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
            ErrorReporter.hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        if (ErrorReporter.hadError) return
        val parser = Parser(tokens)
        if (ErrorReporter.hadError) return
        val statements = parser.parse()
        if (ErrorReporter.hadError) return
        val resolver = Resolver(interpreter)
        resolver.resolve(statements)
        if (ErrorReporter.hadError) return
        interpreter.interpret(statements)
    }
}
