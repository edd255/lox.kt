package dev.edd255.lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReturnTest {
    @Test
    fun `does not allocate stack traces for function return control flow`() {
        val returnValue = Return("value")

        assertEquals("value", returnValue.value)
        assertTrue(returnValue.stackTrace.isEmpty())
    }
}
