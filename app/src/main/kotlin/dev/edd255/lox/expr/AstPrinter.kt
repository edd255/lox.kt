package dev.edd255.lox.expr

class AstPrinter : ExprVisitor<String> {
    fun print(expr: Expr): String = expr.accept(this)

    override fun visitBinaryExpr(expr: Binary): String = parenthesize(
        expr.op.lexeme,
        expr.left,
        expr.right,
    )

    override fun visitGroupingExpr(expr: Grouping): String = parenthesize("group", expr.expr)

    override fun visitLiteralExpr(expr: Literal): String = expr.value.toString()

    override fun visitLogicalExpr(expr: Logical): String = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitUnaryExpr(expr: Unary): String = parenthesize(expr.op.lexeme, expr.right)

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    override fun visitVariableExpr(expr: Variable): String = parenthesize("var '${expr.name.lexeme}'")

    override fun visitAssignExpr(expr: Assign): String = parenthesize("assign '${expr.name.lexeme}'", expr.value)
}
