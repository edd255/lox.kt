package dev.edd255.lox

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import kotlin.test.assertFalse

data class LoxRunResult(
    val stdout: String,
    val stderr: String,
    val hadError: Boolean,
    val hadRuntimeError: Boolean,
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
    val output = captureOutput {
        resetErrorReporter()
        tokens = Scanner(source).scanTokens()
        hadError = ErrorReporter.hadError
    }
    return ScanResult(tokens, output.stderr, hadError)
}

fun parseStatements(source: String): List<Statement> {
    resetErrorReporter()
    val tokens = Scanner(source).scanTokens()
    assertFalse(ErrorReporter.hadError, "scanner failed for source:\n$source")
    val statements = Parser(tokens).parse()
    assertFalse(ErrorReporter.hadError, "parser failed for source:\n$source")
    return statements
}

fun runLox(source: String): LoxRunResult {
    var hadError = false
    var hadRuntimeError = false
    val output = captureOutput {
        resetErrorReporter()
        val tokens = Scanner(source).scanTokens()
        if (!ErrorReporter.hadError) {
            val statements = Parser(tokens).parse()
            if (!ErrorReporter.hadError) {
                val interpreter = Interpreter()
                Resolver(interpreter).resolve(statements)
                if (!ErrorReporter.hadError) {
                    interpreter.interpret(statements)
                }
            }
        }
        hadError = ErrorReporter.hadError
        hadRuntimeError = ErrorReporter.hadRuntimeError
    }
    return LoxRunResult(output.stdout, output.stderr, hadError, hadRuntimeError)
}

fun resetErrorReporter() {
    ErrorReporter.hadError = false
    ErrorReporter.hadRuntimeError = false
}

private fun captureOutput(block: () -> Unit): CapturedOutput {
    val originalOut = System.out
    val originalErr = System.err
    val stdout = ByteArrayOutputStream()
    val stderr = ByteArrayOutputStream()
    try {
        System.setOut(PrintStream(stdout, true, StandardCharsets.UTF_8.name()))
        System.setErr(PrintStream(stderr, true, StandardCharsets.UTF_8.name()))
        block()
    } finally {
        System.out.flush()
        System.err.flush()
        System.setOut(originalOut)
        System.setErr(originalErr)
    }
    return CapturedOutput(stdout.toUtf8String(), stderr.toUtf8String())
}

private fun ByteArrayOutputStream.toUtf8String(): String =
    String(toByteArray(), StandardCharsets.UTF_8).replace("\r\n", "\n")
