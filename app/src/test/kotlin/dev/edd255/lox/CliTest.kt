package dev.edd255.lox

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliTest {
    @Test
    fun `usage errors return 64`() {
        var exitCode = -1
        val output = captureOutput {
            exitCode = runCli(arrayOf("one.lox", "two.lox"))
        }

        assertEquals(64, exitCode)
        assertEquals("", output.stdout)
        assertTrue(output.stderr.contains("Usage: lox [options] [script]"), output.stderr)
    }

    @Test
    fun `compile errors return 65`() {
        val file = writeTempLox("var broken = ;")
        try {
            var exitCode = -1
            val output = captureOutput {
                exitCode = runCli(arrayOf(file.toString()))
            }

            assertEquals(65, exitCode)
            assertEquals("", output.stdout)
            assertTrue(output.stderr.contains("Error"), output.stderr)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    @Test
    fun `runtime errors return 70`() {
        val file = writeTempLox("print missing;")
        try {
            var exitCode = -1
            val output = captureOutput {
                exitCode = runCli(arrayOf(file.toString()))
            }

            assertEquals(70, exitCode)
            assertEquals("", output.stdout)
            assertTrue(output.stderr.contains("Undefined variable 'missing'"), output.stderr)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    @Test
    fun `native exit returns requested code`() {
        val file = writeTempLox("exit(7);")
        try {
            var exitCode = -1
            val output = captureOutput {
                exitCode = runCli(arrayOf(file.toString()))
            }

            assertEquals(7, exitCode)
            assertEquals("", output.stdout)
            assertEquals("", output.stderr)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    @Test
    fun `source files are read as utf8`() {
        val file = writeTempLox(
            """
            var café = "crème brûlée";
            print café;
            """.trimIndent()
        )
        try {
            var exitCode = -1
            val output = captureOutput {
                exitCode = runCli(arrayOf(file.toString()))
            }

            assertEquals(0, exitCode)
            assertEquals("crème brûlée\n", output.stdout)
            assertEquals("", output.stderr)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    @Test
    fun `quiet repl preserves state between input lines`() {
        var exitCode = -1
        val output = captureOutput(
            """
            var greeting = "hello";
            print greeting;
            """.trimIndent()
        ) {
            exitCode = runCli(arrayOf("--quiet"))
        }

        assertEquals(0, exitCode)
        assertEquals(">>> >>> hello\n>>> ", output.stdout)
        assertEquals("", output.stderr)
    }

    @Test
    fun `help and version return success`() {
        var helpExitCode = -1
        val helpOutput = captureOutput {
            helpExitCode = runCli(arrayOf("--help"))
        }

        var versionExitCode = -1
        val versionOutput = captureOutput {
            versionExitCode = runCli(arrayOf("--version"))
        }

        assertEquals(0, helpExitCode)
        assertTrue(helpOutput.stdout.contains("Usage: lox [options] [script]"), helpOutput.stdout)
        assertEquals("", helpOutput.stderr)
        assertEquals(0, versionExitCode)
        assertEquals("lox.kt 0.1.0\n", versionOutput.stdout)
        assertEquals("", versionOutput.stderr)
    }

    private fun writeTempLox(source: String): Path {
        val file = Files.createTempFile("lox-", ".lox")
        Files.writeString(file, source, StandardCharsets.UTF_8)
        return file
    }
}
