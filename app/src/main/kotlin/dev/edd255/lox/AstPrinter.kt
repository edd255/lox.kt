package dev.edd255.lox

class AstPrinter : Expression.Visitor<String> {
    fun print(expression: Expression): String = expression.accept(this)

    override fun visitBinaryExpression(expression: Expression.Binary): String = parenthesize(
        expression.operator.lexeme,
        expression.left,
        expression.right,
    )

    override fun visitGroupingExpression(expression: Expression.Grouping): String = parenthesize("group", expression.expression)

    override fun visitLiteralExpression(expression: Expression.Literal): String = expression.value.toString()

    override fun visitLogicalExpression(expression: Expression.Logical): String = parenthesize(expression.operator.lexeme, expression.left, expression.right)

    override fun visitUnaryExpression(expression: Expression.Unary): String = parenthesize(expression.operator.lexeme, expression.right)

    override fun visitVariableExpression(expression: Expression.Variable): String = parenthesize("var '${expression.name.lexeme}'")

    override fun visitAssignExpression(expression: Expression.Assign): String = parenthesize("assign '${expression.name.lexeme}'", expression.value)

    private fun parenthesize(name: String, vararg expressions: Expression): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        for (expr in expressions) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }
}
