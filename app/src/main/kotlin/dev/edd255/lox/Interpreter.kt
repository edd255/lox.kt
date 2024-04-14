package dev.edd255.lox

import dev.edd255.lox.expr.*

class Interpreter : ExprVisitor<Any?>, StmtVisitor<Unit> {
    private val errorReporter = ErrorReporter()
    private var environment = Env()

    fun interpret(stmts: List<Stmt>) {
        try {
            for (stmt in stmts) {
                execute(stmt)
            }
        } catch (error: RuntimeError) {
            errorReporter.runtimeError(error)
        }
    }

    override fun visitAssignExpr(expr: Assign): Any? {
        val value = evaluate(expr.value)
        value?.let { environment.assign(expr.name, it) }
        return value
    }

    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        return when (expr.op.type) {
            TokenType.MINUS -> {
                if (left is Double && right is Double) {
                    left - right
                } else {
                    throw RuntimeError(
                        expr.op,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.SLASH -> {
                if (left is Double && right is Double) {
                    left / right
                } else {
                    throw RuntimeError(
                        expr.op,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.STAR -> {
                if (left is Double && right is Double) {
                    left * right
                } else {
                    throw RuntimeError(
                        expr.op,
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
                    throw RuntimeError(expr.op, "Operands must be either numbers or strings")
                }
            }
            TokenType.GREATER -> {
                if (left is Double && right is Double) {
                    left > right
                } else {
                    throw RuntimeError(
                        expr.op,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.GREATER_EQUAL -> {
                if (left is Double && right is Double) {
                    left >= right
                } else {
                    throw RuntimeError(
                        expr.op,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS -> {
                if (left is Double && right is Double) {
                    left < right
                } else {
                    throw RuntimeError(
                        expr.op,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS_EQUAL -> {
                if (left is Double && right is Double) {
                    left <= right
                } else {
                    throw RuntimeError(
                        expr.op,
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

    override fun visitGroupingExpr(expr: Grouping): Any? = evaluate(expr.expr)

    override fun visitLiteralExpr(expr: Literal): Any? = expr.value

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.op.type) {
            TokenType.MINUS -> {
                if (right is Double) -right else null
            }
            TokenType.BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitVariableExpr(expr: Variable): Any? {
        return environment.get(expr.name)
    }

    private fun evaluate(expr: Expr?): Any? {
        assert(expr != null)
        if (expr == null) return null
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt?) {
        stmt?.accept(this)
    }

    private fun executeBlock(stmts: List<Stmt>, env: Env) {
        val previous = this.environment
        try {
            this.environment = env
            for (stmt in stmts) {
                execute(stmt)
            }
        } finally {
            this.environment = previous
        }
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

    override fun visitBlockStmt(stmt: Block) {
        executeBlock(stmt.stmts, Env(environment))
    }

    override fun visitExprStmt(stmt: ExprStmt) {
        evaluate(stmt.expr)
    }

    override fun visitPrintStmt(stmt: Print) {
        val value = evaluate(stmt.expr)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Var) {
        val value = evaluate(stmt.initializer)
        value?.let { environment.define(stmt.name.lexeme, it) }
    }

    override fun visitIfStmt(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else {
            execute(stmt.elseBranch)
        }
    }

    override fun visitLogicalExpr(expr: Logical): Any? {
        val left = evaluate(expr.left)
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left
            }
        } else {
            if (!isTruthy(left)) {
                return left
            }
        }
        return evaluate(expr.right)
    }

    override fun visitWhileStmt(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }
}
