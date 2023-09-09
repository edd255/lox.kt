package dev.edd255.lox.expr

class AstPrinter : ExprVisitor<String> {
    fun print(expr: Expr): String = expr.accept(this)

    override fun visitBinaryExpr(expr: Binary): String = parenthesize(
        expr.getOp().getLexeme(),
        expr.getLeft(),
        expr.getRight(),
    )

    override fun visitGroupingExpr(expr: Grouping): String = parenthesize("group", expr.getExpr())

    override fun visitLiteralExpr(expr: Literal): String = expr.getValue().toString()

    override fun visitUnaryExpr(expr: Unary): String = parenthesize(expr.getOp().getLexeme(), expr.getRight())

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
}
