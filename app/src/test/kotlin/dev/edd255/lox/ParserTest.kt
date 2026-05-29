package dev.edd255.lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserTest {
    @Test
    fun `parses expression precedence`() {
        val print = assertIs<Statement.Print>(parseStatements("print 1 + 2 * 3 == 7;").single())
        val equality = assertIs<Expression.Binary>(print.expression)

        assertEquals(TokenType.EQUAL_EQUAL, equality.operator.type)
        val addition = assertIs<Expression.Binary>(equality.left)
        assertEquals(TokenType.PLUS, addition.operator.type)
        val multiplication = assertIs<Expression.Binary>(addition.right)
        assertEquals(TokenType.STAR, multiplication.operator.type)
        assertEquals(7.0, assertIs<Expression.Literal>(equality.right).value)
    }

    @Test
    fun `parses property assignment target`() {
        val statement = assertIs<Statement.ExpressionStatement>(parseStatements("object.field = value;").single())
        val set = assertIs<Expression.Set>(statement.expression)

        assertEquals("field", set.name.lexeme)
        assertEquals("object", assertIs<Expression.Variable>(set.obj).name.lexeme)
        assertEquals("value", assertIs<Expression.Variable>(set.value).name.lexeme)
    }

    @Test
    fun `desugars for loops into block and while statements`() {
        val statement = parseStatements("for (var i = 0; i < 2; i = i + 1) print i;").single()
        val block = assertIs<Statement.Block>(statement)

        assertEquals(2, block.statements.size)
        assertIs<Statement.Variable>(block.statements[0])
        val whileStatement = assertIs<Statement.While>(block.statements[1])
        val body = assertIs<Statement.Block>(whileStatement.body)
        assertEquals(2, body.statements.size)
        assertIs<Statement.Print>(body.statements[0])
        assertIs<Statement.ExpressionStatement>(body.statements[1])
    }

    @Test
    fun `parses class declarations with superclass and methods`() {
        val klass = assertIs<Statement.Class>(
            parseStatements("class Doughnut: Pastry { fn cook(flavor) { return this; } }").single()
        )

        assertEquals("Doughnut", klass.name.lexeme)
        assertEquals("Pastry", klass.superclass?.name?.lexeme)
        val method = klass.methods.single()
        assertEquals("cook", method.name.lexeme)
        assertEquals(listOf("flavor"), method.parameters.map { it.lexeme })
        assertIs<Statement.Return>(method.body.single())
    }
}
