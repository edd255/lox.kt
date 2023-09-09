package dev.edd255.lox

import dev.edd255.lox.expr.Binary
import dev.edd255.lox.expr.Expr
import dev.edd255.lox.expr.Grouping
import dev.edd255.lox.expr.Literal
import dev.edd255.lox.expr.Unary
import dev.edd255.lox.expr.ExprVisitor

class Interpreter : ExprVisitor<Any?> {
    private val errorReporter = ErrorReporter()

    fun interpret(expr: Expr?) {
        try {
            val value = evaluate(expr)
            println(stringify(value))
        } catch (error: RuntimeError) {
            errorReporter.runtimeError(error)
        }
    }

    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = evaluate(expr.getLeft())
        val right = evaluate(expr.getRight())
        return when (expr.getOp().getType()) {
            TokenType.MINUS -> {
                if (left is Double && right is Double) {
                    left - right
                } else {
                    throw RuntimeError(
                        expr.getOp(),
                        "Operand must be a number",
                    )
                }
            }
            TokenType.SLASH -> {
                if (left is Double && right is Double) {
                    left / right
                } else {
                    throw RuntimeError(
                        expr.getOp(),
                        "Operand must be a number",
                    )
                }
            }
            TokenType.STAR -> {
                if (left is Double && right is Double) {
                    left * right
                } else {
                    throw RuntimeError(
                        expr.getOp(),
                        "Operand must be a number",
                    )
                }
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expr.getOp(), "Operands must be either numbers or strings")
                }
            }
            TokenType.GREATER -> {
                if (left is Double && right is Double) {
                    left > right
                } else {
                    throw RuntimeError(
                        expr.getOp(),
                        "Operand must be a number",
                    )
                }
            }
            TokenType.GREATER_EQUAL -> {
                if (left is Double && right is Double) {
                    left >= right
                } else {
                    throw RuntimeError(
                        expr.getOp(),
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS -> {
                if (left is Double && right is Double) {
                    left < right
                } else {
                    throw RuntimeError(
                        expr.getOp(),
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS_EQUAL -> {
                if (left is Double && right is Double) {
                    left <= right
                } else {
                    throw RuntimeError(
                        expr.getOp(),
                        "Operand must be a number",
                    )
                }
            }
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            else -> {
                null
            }
        }
    }

    override fun visitGroupingExpr(expr: Grouping): Any? = evaluate(expr.getExpr())

    override fun visitLiteralExpr(expr: Literal): Any? = expr.getValue()

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = evaluate(expr.getRight())
        return when (expr.getOp().getType()) {
            TokenType.MINUS -> {
                if (right is Double) -1 * right else null
            }
            TokenType.BANG -> !isTruthy(right)
            else -> null
        }
    }

    private fun evaluate(expr: Expr?): Any? {
        assert(expr != null)
        if (expr == null) return null
        return expr.accept(this)
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        return when (obj) {
            is Boolean -> obj
            else -> false
        }
    }

    private fun isEqual(a: Any?, b: Any?): Boolean = a == b

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"
        return when (obj) {
            is Double -> {
                var text = obj.toString()
                if (text.endsWith(".0")) {
                    text = text.substring(0, text.length - 2)
                }
                text
            }
            else -> obj.toString()
        }
    }
}
