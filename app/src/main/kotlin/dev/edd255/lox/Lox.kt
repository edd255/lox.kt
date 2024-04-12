package dev.edd255.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    private val errorReporter = ErrorReporter()
    private val interpreter = Interpreter()

    fun runFile(path: String) {
        val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (errorReporter.getHadError()) {
            exitProcess(65)
        }
        if (errorReporter.getHadRuntimeError()) {
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
            errorReporter.setHadError(false)
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        val parser = Parser(tokens)
        val stmts = parser.parse()
        if (errorReporter.getHadError()) {
            return
        }
        if (stmts != null) {
            interpreter.interpret(stmts)
        }
    }
}
