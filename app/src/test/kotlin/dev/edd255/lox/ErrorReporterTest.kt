package dev.edd255.lox

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ErrorReporterTest {
    @Test
    fun `tracks compile error state per reporter instance`() {
        val first = ErrorReporter()
        val second = ErrorReporter()

        captureStderr {
            first.error(1, "bad token")
        }

        assertTrue(first.hadError)
        assertFalse(first.hadRuntimeError)
        assertFalse(second.hadError)
        assertFalse(second.hadRuntimeError)

        first.reset()

        assertFalse(first.hadError)
        assertFalse(first.hadRuntimeError)
    }

    @Test
    fun `tracks runtime error state per reporter instance`() {
        val first = ErrorReporter()
        val second = ErrorReporter()
        val token = Token(TokenType.IDENTIFIER, "missing", null, 3)

        captureStderr {
            first.runtimeError(RuntimeError(token, "Undefined variable 'missing'"))
        }

        assertFalse(first.hadError)
        assertTrue(first.hadRuntimeError)
        assertFalse(second.hadError)
        assertFalse(second.hadRuntimeError)
    }

    private fun captureStderr(block: () -> Unit) {
        val originalErr = System.err
        try {
            System.setErr(PrintStream(ByteArrayOutputStream(), true, StandardCharsets.UTF_8.name()))
            block()
        } finally {
            System.setErr(originalErr)
        }
    }
}
