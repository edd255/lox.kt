package dev.edd255.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    private val errorReporter = ErrorReporter()

    fun runFile(path: String) {
        val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (errorReporter.hadError) {
            exitProcess(65)
        }
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print(">>> ")
            val line = reader.readLine() ?: break
            run(line)
            errorReporter.hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        for (token in tokens) {
            println(token)
        }
    }
}