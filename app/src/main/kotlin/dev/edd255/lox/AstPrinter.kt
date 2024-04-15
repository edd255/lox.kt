package dev.edd255.lox

class AstPrinter : Expression.Visitor<String> {
    fun print(expression: Expression): String = expression.accept(this)

    override fun visitBinaryExpression(binary: Expression.Binary): String = parenthesize(
        binary.operator.lexeme,
        binary.left,
        binary.right,
    )

    override fun visitCallExpression(call: Expression.Call): String = parenthesize("call", call.callee, *call.arguments.toTypedArray())

    override fun visitGetExpression(get: Expression.Get): String = parenthesize("get '${get.name.lexeme}'", get.obj)

    override fun visitGroupingExpression(grouping: Expression.Grouping): String = parenthesize("group", grouping.expression)

    override fun visitLiteralExpression(literal: Expression.Literal): String = literal.value.toString()

    override fun visitLogicalExpression(logical: Expression.Logical): String = parenthesize(logical.operator.lexeme, logical.left, logical.right)

    override fun visitSetExpression(set: Expression.Set): String = parenthesize("set '${set.name.lexeme}'", set.obj, set.value)

    override fun visitSuperExpression(superExpression: Expression.Super): String = parenthesize("super '${superExpression.method.lexeme}'")

    override fun visitThisExpression(thisStatement: Expression.This): String = "this"

    override fun visitUnaryExpression(unary: Expression.Unary): String = parenthesize(unary.operator.lexeme, unary.right)

    override fun visitVariableExpression(variable: Expression.Variable): String = parenthesize("var '${variable.name.lexeme}'")

    override fun visitAssignExpression(assign: Expression.Assign): String = parenthesize("assign '${assign.name.lexeme}'", assign.value)

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
