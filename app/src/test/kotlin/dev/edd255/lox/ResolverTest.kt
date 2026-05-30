package dev.edd255.lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResolverTest {
    @Test
    fun `reports top level return`() {
        val result = runLox("return 1;")

        assertTrue(result.hadError)
        assertFalse(result.hadRuntimeError)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("Cannot return from top-level code."), result.stderr)
    }

    @Test
    fun `reports local variable read in its own initializer`() {
        val result = runLox("{ var value = value; }")

        assertTrue(result.hadError)
        assertFalse(result.hadRuntimeError)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("Cannot read local variable in its own initializer."), result.stderr)
    }

    @Test
    fun `reports this outside of a class`() {
        val result = runLox("print this;")

        assertTrue(result.hadError)
        assertFalse(result.hadRuntimeError)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("Cannot use 'this' outside of a class."), result.stderr)
    }

    @Test
    fun `reports super in class without superclass`() {
        val result = runLox("class A { fn method() { return super.method(); } }")

        assertTrue(result.hadError)
        assertFalse(result.hadRuntimeError)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("Cannot use 'super' in a class with no superclass."), result.stderr)
    }

    @Test
    fun `reports class self inheritance`() {
        val result = runLox("class A: A {}")

        assertTrue(result.hadError)
        assertFalse(result.hadRuntimeError)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("A class cannot inherit from itself."), result.stderr)
    }

    @Test
    fun `restores class state after self inheritance errors`() {
        val result = runLox(
            """
            class A: A {}
            print this;
            """.trimIndent()
        )

        assertTrue(result.hadError)
        assertFalse(result.hadRuntimeError)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("A class cannot inherit from itself."), result.stderr)
        assertTrue(result.stderr.contains("Cannot use 'this' outside of a class."), result.stderr)
    }
}
