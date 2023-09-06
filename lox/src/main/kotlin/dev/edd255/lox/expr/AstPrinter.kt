package dev.edd255.lox.expr

class AstPrinter : Visitor<String> {
    fun print(expr: Expr) : String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(expr: Binary): String {
        return parenthesize(expr.op.lexeme, expr.left, expr.right)
    }

    override fun visitGroupingExpr(expr: Grouping): String {
        return parenthesize("group", expr.expr)
    }

    override fun visitLiteralExpr(expr: Literal): String {
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Unary): String {
        return parenthesize(expr.op.lexeme, expr.right)
    }

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