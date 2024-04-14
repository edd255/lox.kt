package dev.edd255.lox

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    private val errorReporter = ErrorReporter()
    private var environment = Environment()

    fun interpret(statements: List<Statement>) {
        try {
            for (stmt in statements) {
                execute(stmt)
            }
        } catch (error: RuntimeError) {
            errorReporter.runtimeError(error)
        }
    }

    override fun visitAssignExpression(expression: Expression.Assign): Any? {
        val value = evaluate(expression.value)
        value?.let { environment.assign(expression.name, it) }
        return value
    }

    override fun visitBinaryExpression(expression: Expression.Binary): Any? {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)
        return when (expression.operator.type) {
            TokenType.MINUS -> {
                if (left is Double && right is Double) {
                    left - right
                } else {
                    throw RuntimeError(
                        expression.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.SLASH -> {
                if (left is Double && right is Double) {
                    left / right
                } else {
                    throw RuntimeError(
                        expression.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.STAR -> {
                if (left is Double && right is Double) {
                    left * right
                } else {
                    throw RuntimeError(
                        expression.operator,
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
                    throw RuntimeError(expression.operator, "Operands must be either numbers or strings")
                }
            }
            TokenType.GREATER -> {
                if (left is Double && right is Double) {
                    left > right
                } else {
                    throw RuntimeError(
                        expression.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.GREATER_EQUAL -> {
                if (left is Double && right is Double) {
                    left >= right
                } else {
                    throw RuntimeError(
                        expression.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS -> {
                if (left is Double && right is Double) {
                    left < right
                } else {
                    throw RuntimeError(
                        expression.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS_EQUAL -> {
                if (left is Double && right is Double) {
                    left <= right
                } else {
                    throw RuntimeError(
                        expression.operator,
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

    override fun visitGroupingExpression(expression: Expression.Grouping): Any? = evaluate(expression.expression)

    override fun visitLiteralExpression(expression: Expression.Literal): Any? = expression.value

    override fun visitUnaryExpression(expression: Expression.Unary): Any? {
        val right = evaluate(expression.right)
        return when (expression.operator.type) {
            TokenType.MINUS -> {
                if (right is Double) -right else null
            }
            TokenType.BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitVariableExpression(expression: Expression.Variable): Any? {
        return environment.get(expression.name)
    }

    private fun evaluate(expression: Expression?): Any? {
        assert(expression != null)
        if (expression == null) return null
        return expression.accept(this)
    }

    private fun execute(statement: Statement?) {
        statement?.accept(this)
    }

    private fun executeBlock(statements: List<Statement>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (stmt in statements) {
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

    override fun visitBlockStatement(statement: Statement.Block) {
        executeBlock(statement.statements, Environment(environment))
    }

    override fun visitExpressionStatement(statement: Statement.ExpressionStatement) {
        evaluate(statement.expression)
    }

    override fun visitPrintStatement(statement: Statement.Print) {
        val value = evaluate(statement.expression)
        println(stringify(value))
    }

    override fun visitVarStatement(statement: Statement.Variable) {
        val value = evaluate(statement.initializer)
        value?.let { environment.define(statement.name.lexeme, it) }
    }

    override fun visitIfStatement(statement: Statement.If) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenBranch)
        } else {
            execute(statement.elseBranch)
        }
    }

    override fun visitLogicalExpression(expression: Expression.Logical): Any? {
        val left = evaluate(expression.left)
        if (expression.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left
            }
        } else {
            if (!isTruthy(left)) {
                return left
            }
        }
        return evaluate(expression.right)
    }

    override fun visitWhileStatement(statement: Statement.While) {
        while (isTruthy(evaluate(statement.condition))) {
            execute(statement.body)
        }
    }
}
