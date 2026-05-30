package dev.edd255.lox

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.fail

class LoxFileTest {
    private val ansiBold = "\u001B[1m"
    private val ansiGreen = "\u001B[32m"
    private val ansiRed = "\u001B[31m"
    private val ansiReset = "\u001B[0m"

    @Test
    fun `lox files match expectations`() {
        val root = loxTestRoot()
        val files = loxFiles(root)
        if (files.isEmpty()) fail("No Lox test files found in $root")

        val results = files.map { file ->
            val source = Files.readString(file)
            checkFile(root, file, Expectation.parse(source), runLox(source))
        }
        printSummary(root, results)

        val failures = results.filterNot { it.passed }.map { it.failureReport() }
        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n\n"))
        }
    }

    @Test
    fun `lox lox matches lox file expectations`() {
        val root = loxTestRoot()
        val interpreter = root.resolve("lox.lox")
        if (!Files.isRegularFile(interpreter)) fail("Could not find $interpreter")

        val interpreterSource = Files.readString(interpreter)
        val files = loxFiles(root)
        if (files.isEmpty()) fail("No Lox test files found in $root")

        val results = files.map { file ->
            val source = Files.readString(file)
            checkFile(
                root,
                file,
                Expectation.parse(source),
                runLox(interpreterSource, stdin = source),
                treatExitCodesAsErrors = true,
                // lox.lox exits on the first parser error; the Kotlin parser may report several.
                requireAllErrorSnippets = false,
            )
        }
        printSummary(root, results, "Lox-in-Lox file tests")

        val failures = results.filterNot { it.passed }.map { it.failureReport() }
        if (failures.isNotEmpty()) {
            fail(failures.joinToString("\n\n"))
        }
    }

    private fun checkFile(
        root: Path,
        file: Path,
        expectation: Expectation,
        result: LoxRunResult,
        treatExitCodesAsErrors: Boolean = false,
        requireAllErrorSnippets: Boolean = true,
    ): FileResult {
        val relativePath = root.relativize(file)
        val failures = mutableListOf<String>()
        val hadCompileError = result.hadError || (treatExitCodesAsErrors && result.exitCode == 65)
        val hadRuntimeError = result.hadRuntimeError || (treatExitCodesAsErrors && result.exitCode == 70)
        if (expectation.isEmpty()) {
            failures.add("has no expectation comments")
        }
        if (expectation.expectsCompileError && expectation.expectsRuntimeError) {
            failures.add("expects both compile and runtime errors")
        }

        when {
            expectation.expectsCompileError -> {
                if (!hadCompileError) failures.add("expected a compile error")
                if (hadRuntimeError) failures.add("expected no runtime error")
            }
            expectation.expectsRuntimeError -> {
                if (hadCompileError) failures.add("expected no compile error")
                if (!hadRuntimeError) failures.add("expected a runtime error")
            }
            else -> {
                if (hadCompileError) failures.add("expected no compile error")
                if (hadRuntimeError) failures.add("expected no runtime error")
            }
        }

        val expectedStdout = expectation.stdoutLines.toOutput()
        if (result.stdout != expectedStdout) {
            failures.add(
                """
                stdout differed
                expected:
                ${expectedStdout.showOutput()}
                actual:
                ${result.stdout.showOutput()}
                """.trimIndent()
            )
        }

        if (expectation.stderrLines.isNotEmpty()) {
            val expectedStderr = expectation.stderrLines.toOutput()
            if (result.stderr != expectedStderr) {
                failures.add(
                    """
                    stderr differed
                    expected:
                    ${expectedStderr.showOutput()}
                    actual:
                    ${result.stderr.showOutput()}
                    """.trimIndent()
                )
            }
        } else if (!expectation.expectsCompileError && !expectation.expectsRuntimeError && result.stderr.isNotEmpty()) {
            failures.add(
                """
                expected empty stderr
                actual:
                ${result.stderr.showOutput()}
                """.trimIndent()
            )
        }

        val errorSnippets = expectation.compileErrorSnippets + expectation.runtimeErrorSnippets
        if (requireAllErrorSnippets) {
            for (snippet in errorSnippets) {
                if (!result.stderr.contains(snippet)) {
                    failures.add(
                        """
                        stderr did not contain:
                        $snippet
                        actual:
                        ${result.stderr.showOutput()}
                        """.trimIndent()
                    )
                }
            }
        } else if (errorSnippets.isNotEmpty() && errorSnippets.none { result.stderr.contains(it) }) {
            failures.add(
                """
                stderr did not contain any expected error snippet:
                ${errorSnippets.joinToString("\n")}
                actual:
                ${result.stderr.showOutput()}
                """.trimIndent()
            )
        }

        return FileResult(relativePath, failures)
    }

    private fun printSummary(root: Path, results: List<FileResult>, title: String = "Lox file tests") {
        println()
        println(bold("$title: ${root.toAbsolutePath().normalize()}"))

        for ((directory, directoryResults) in results.groupBy { it.directory }) {
            println()
            println(bold("[$directory]"))
            directoryResults.forEach { result ->
                println("${formatStatus(result)} ${result.relativePath.fileName}")
            }
            val passed = directoryResults.count { it.passed }
            println("${bold("Summary:")} $passed/${directoryResults.size} passed")
        }

        val passed = results.count { it.passed }
        println()
        println("${bold("Total:")} $passed/${results.size} passed")
    }

    private data class FileResult(
        val relativePath: Path,
        val failures: List<String>,
    ) {
        val passed: Boolean = failures.isEmpty()
        val status: String = if (passed) "PASS" else "FAIL"
        val directory: String = relativePath.parent?.toString() ?: "."

        fun failureReport(): String = buildString {
            append(relativePath)
            appendLine(":")
            failures.forEach { failure ->
                appendLine(failure.prependIndent("  "))
            }
        }.trimEnd()
    }

    private fun loxTestRoot(): Path =
        listOf(
            Paths.get("src/test/lox"),
            Paths.get("app/src/test/lox"),
        ).firstOrNull { Files.isDirectory(it) }
            ?: fail("Could not find Lox test root")

    private fun loxFiles(root: Path): List<Path> {
        val stream = Files.walk(root)
        return try {
            stream
                .filter { Files.isRegularFile(it) && it.name.endsWith(".lox") && it.name != "lox.lox" }
                .sorted()
                .toList()
        } finally {
            stream.close()
        }
    }

    private data class Expectation(
        val stdoutLines: List<String>,
        val stderrLines: List<String>,
        val compileErrorSnippets: List<String>,
        val runtimeErrorSnippets: List<String>,
    ) {
        val expectsCompileError: Boolean = compileErrorSnippets.isNotEmpty()
        val expectsRuntimeError: Boolean = runtimeErrorSnippets.isNotEmpty()

        fun isEmpty(): Boolean =
            stdoutLines.isEmpty() &&
                stderrLines.isEmpty() &&
                compileErrorSnippets.isEmpty() &&
                runtimeErrorSnippets.isEmpty()

        companion object {
            fun parse(source: String): Expectation {
                val stdoutLines = mutableListOf<String>()
                val stderrLines = mutableListOf<String>()
                val compileErrorSnippets = mutableListOf<String>()
                val runtimeErrorSnippets = mutableListOf<String>()

                for (line in source.lineSequence()) {
                    var comment = line.substringAfter("//", missingDelimiterValue = "").trimStart()
                    while (comment.startsWith("//")) {
                        comment = comment.removePrefix("//").trimStart()
                    }
                    if (!comment.startsWith("expect")) continue

                    val rest = comment.removePrefix("expect").trimStart()
                    when {
                        rest.startsWith(":") -> stdoutLines.add(rest.removePrefix(":").trimStart())
                        rest.startsWith("stderr:") -> stderrLines.add(rest.removePrefix("stderr:").trimStart())
                        rest.startsWith("compile error:") -> {
                            compileErrorSnippets.add(rest.removePrefix("compile error:").trimStart())
                        }
                        rest.startsWith("runtime error:") -> {
                            runtimeErrorSnippets.add(rest.removePrefix("runtime error:").trimStart())
                        }
                        else -> fail("Unsupported expectation comment: //$comment")
                    }
                }

                for (line in source.lineSequence()) {
                    var comment = line.substringAfter("//", missingDelimiterValue = "").trimStart()
                    while (comment.startsWith("//")) {
                        comment = comment.removePrefix("//").trimStart()
                    }
                    when {
                        comment.startsWith("[line ") -> compileErrorSnippets.add(comment)
                        comment.startsWith("Error") -> compileErrorSnippets.add(comment)
                    }
                }

                return Expectation(stdoutLines, stderrLines, compileErrorSnippets, runtimeErrorSnippets)
            }
        }
    }

    private fun List<String>.toOutput(): String =
        if (isEmpty()) "" else joinToString(separator = "\n", postfix = "\n")

    private fun String.showOutput(): String =
        if (isEmpty()) "<empty>" else this

    private fun formatStatus(result: FileResult): String =
        if (result.passed) greenBold(result.status) else redBold(result.status)

    private fun bold(text: String): String = "$ansiBold$text$ansiReset"

    private fun greenBold(text: String): String = "$ansiBold$ansiGreen$text$ansiReset"

    private fun redBold(text: String): String = "$ansiBold$ansiRed$text$ansiReset"
}
