package dev.edd255.lox

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import kotlin.test.assertFalse

data class LoxRunResult(
    val stdout: String,
    val stderr: String,
    val hadError: Boolean,
    val hadRuntimeError: Boolean,
    val exitCode: Int? = null,
)

data class ScanResult(
    val tokens: List<Token>,
    val stderr: String,
    val hadError: Boolean,
)

private data class CapturedOutput(val stdout: String, val stderr: String)

fun scanSource(source: String): ScanResult {
    lateinit var tokens: List<Token>
    var hadError = false
    val errorReporter = ErrorReporter()
    val output = captureOutput {
        tokens = Scanner(source, errorReporter).scanTokens()
        hadError = errorReporter.hadError
    }
    return ScanResult(tokens, output.stderr, hadError)
}

fun parseStatements(source: String): List<Statement> {
    val errorReporter = ErrorReporter()
    val tokens = Scanner(source, errorReporter).scanTokens()
    assertFalse(errorReporter.hadError, "scanner failed for source:\n$source")
    val statements = Parser(tokens, errorReporter).parse()
    assertFalse(errorReporter.hadError, "parser failed for source:\n$source")
    return statements
}

fun runLox(source: String, stdin: String = ""): LoxRunResult {
    var hadError = false
    var hadRuntimeError = false
    var exitCode: Int? = null
    val errorReporter = ErrorReporter()
    val output = captureOutput(stdin) {
        try {
            val tokens = Scanner(source, errorReporter).scanTokens()
            if (!errorReporter.hadError) {
                val statements = Parser(tokens, errorReporter).parse()
                if (!errorReporter.hadError) {
                    val interpreter = Interpreter(errorReporter)
                    Resolver(interpreter, errorReporter).resolve(statements)
                    if (!errorReporter.hadError) {
                        interpreter.interpret(statements)
                    }
                }
            }
        } catch (exit: LoxExit) {
            exitCode = exit.code
        } finally {
            hadError = errorReporter.hadError
            hadRuntimeError = errorReporter.hadRuntimeError
        }
    }
    return LoxRunResult(output.stdout, output.stderr, hadError, hadRuntimeError, exitCode)
}

private fun captureOutput(stdin: String = "", block: () -> Unit): CapturedOutput {
    val originalIn = System.`in`
    val originalOut = System.out
    val originalErr = System.err
    val stdout = ByteArrayOutputStream()
    val stderr = ByteArrayOutputStream()
    try {
        System.setIn(ByteArrayInputStream(stdin.toByteArray(StandardCharsets.UTF_8)))
        System.setOut(PrintStream(stdout, true, StandardCharsets.UTF_8.name()))
        System.setErr(PrintStream(stderr, true, StandardCharsets.UTF_8.name()))
        block()
    } finally {
        System.out.flush()
        System.err.flush()
        System.setIn(originalIn)
        System.setOut(originalOut)
        System.setErr(originalErr)
    }
    return CapturedOutput(stdout.toUtf8String(), stderr.toUtf8String())
}

private fun ByteArrayOutputStream.toUtf8String(): String =
    String(toByteArray(), StandardCharsets.UTF_8).replace("\r\n", "\n")
