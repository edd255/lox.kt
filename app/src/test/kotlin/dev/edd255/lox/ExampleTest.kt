package dev.edd255.lox

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.fail
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class ExampleTest {
    @TestFactory
    fun `examples run without interpreter errors`(): List<DynamicTest> {
        val root = examplesRoot()
        val files = exampleFiles(root)
        if (files.isEmpty()) fail("No example files found in $root")

        return files.map { file ->
            dynamicTest(root.relativize(file).toString()) {
                val result = runLox(Files.readString(file, StandardCharsets.UTF_8))

                assertFalse(result.hadError, result.stderr)
                assertFalse(result.hadRuntimeError, result.stderr)
                assertNull(result.exitCode)
                assertEquals("", result.stderr)
            }
        }
    }

    private fun examplesRoot(): Path =
        listOf(
            Paths.get("examples"),
            Paths.get("../examples"),
        ).firstOrNull { Files.isDirectory(it) }
            ?: fail("Could not find examples directory")

    private fun exampleFiles(root: Path): List<Path> {
        val stream = Files.walk(root)
        return try {
            stream
                .filter { Files.isRegularFile(it) && it.name.endsWith(".lox") }
                .sorted()
                .toList()
        } finally {
            stream.close()
        }
    }
}
