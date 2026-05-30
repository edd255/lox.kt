package dev.edd255.lox

import kotlin.test.Test
import kotlin.test.assertEquals

class AstPrinterTest {
    @Test
    fun `prints statement lists without recursive overflow`() {
        val block = Statement.Block(
            listOf(
                Statement.Print(Expression.Literal("first")),
                Statement.Print(Expression.Literal("second")),
            )
        )

        assertEquals("(block (print first) (print second))", AstPrinter().print(block))
    }
}
