package dev.edd255.lox

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    private val globals = Environment()
    private val locals = hashMapOf<Expression, Int>()
    private var environment = globals

    init {
        globals.define(
            "clock",
            object : LoxCallable {
                override fun arity(): Int = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    return System.currentTimeMillis() / 1000.0
                }

                override fun toString(): String = "<native fn>"
            }
        )
    }

    fun interpret(statements: List<Statement>) {
        try {
            for (stmt in statements) {
                execute(stmt)
            }
        } catch (error: RuntimeError) {
            ErrorReporter.runtimeError(error)
        }
    }

    //==== RESOLVING ===================================================================================================
    fun resolve(expression: Expression, depth: Int) = locals.put(expression, depth)

    private fun lookUpVariable(name: Token, expression: Expression): Any? {
        val distance = locals[expression]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    //==== VISIT EXPRESSIONS ===========================================================================================
    private fun evaluate(expression: Expression?): Any? {
        assert(expression != null)
        if (expression == null) return null
        return expression.accept(this)
    }

    override fun visitAssignExpression(assign: Expression.Assign): Any? {
        val value = evaluate(assign.value)
        val distance = locals[assign]
        if (distance != null) {
            environment.assignAt(distance, assign.name, value)
        } else {
            globals.assign(assign.name, value)
        }
        return value
    }

    override fun visitBinaryExpression(binary: Expression.Binary): Any? {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)
        return when (binary.operator.type) {
            TokenType.MINUS -> {
                if (left is Double && right is Double) {
                    left - right
                } else {
                    throw RuntimeError(
                        binary.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.SLASH -> {
                if (left is Double && right is Double) {
                    left / right
                } else {
                    throw RuntimeError(
                        binary.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.STAR -> {
                if (left is Double && right is Double) {
                    left * right
                } else {
                    throw RuntimeError(
                        binary.operator,
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
                    throw RuntimeError(binary.operator, "Operands must be either numbers or strings")
                }
            }
            TokenType.GREATER -> {
                if (left is Double && right is Double) {
                    left > right
                } else {
                    throw RuntimeError(
                        binary.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.GREATER_EQUAL -> {
                if (left is Double && right is Double) {
                    left >= right
                } else {
                    throw RuntimeError(
                        binary.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS -> {
                if (left is Double && right is Double) {
                    left < right
                } else {
                    throw RuntimeError(
                        binary.operator,
                        "Operand must be a number",
                    )
                }
            }
            TokenType.LESS_EQUAL -> {
                if (left is Double && right is Double) {
                    left <= right
                } else {
                    throw RuntimeError(
                        binary.operator,
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

    override fun visitGroupingExpression(grouping: Expression.Grouping): Any? = evaluate(grouping.expression)

    override fun visitLiteralExpression(literal: Expression.Literal): Any? = literal.value

    override fun visitUnaryExpression(unary: Expression.Unary): Any? {
        val right = evaluate(unary.right)
        return when (unary.operator.type) {
            TokenType.MINUS -> {
                if (right is Double) -right else null
            }
            TokenType.BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitVariableExpression(variable: Expression.Variable): Any? {
        return lookUpVariable(variable.name, variable)
    }

    override fun visitCallExpression(call: Expression.Call): Any? {
        val callee = evaluate(call.callee)
        if (callee !is LoxCallable) {
            throw RuntimeError(call.paren, "Can only call functions and classes")
        }
        val arguments = mutableListOf<Any?>()
        for (argument in call.arguments) {
            arguments.add(evaluate(argument))
        }
        if (arguments.size != callee.arity()) {
            throw RuntimeError(call.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}")
        }
        return callee.call(this, arguments)
    }

    //==== VISIT STATEMENTS ============================================================================================
    private fun execute(statement: Statement?) {
        statement?.accept(this)
    }

    fun executeBlock(statements: List<Statement>, environment: Environment) {
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

    override fun visitBlockStatement(block: Statement.Block) {
        executeBlock(block.statements, Environment(environment))
    }

    override fun visitExpressionStatement(expressionStatement: Statement.ExpressionStatement) {
        evaluate(expressionStatement.expression)
    }

    override fun visitFunctionStatement(function: Statement.Function) {
        val loxFunction = LoxFunction(function, environment)
        environment.define(function.name.lexeme, loxFunction)
    }

    override fun visitPrintStatement(print: Statement.Print) {
        val value = evaluate(print.expression)
        println(stringify(value))
    }

    override fun visitReturnStatement(returnStatement: Statement.Return) {
        throw Return(if (returnStatement.value != null) evaluate(returnStatement.value) else null)
    }

    override fun visitVariableStatement(variable: Statement.Variable) {
        val value = evaluate(variable.initializer)
        value?.let { environment.define(variable.name.lexeme, it) }
    }

    override fun visitIfStatement(ifQuery: Statement.If) {
        if (isTruthy(evaluate(ifQuery.condition))) {
            execute(ifQuery.thenBranch)
        } else {
            execute(ifQuery.elseBranch)
        }
    }

    override fun visitLogicalExpression(logical: Expression.Logical): Any? {
        val left = evaluate(logical.left)
        if (logical.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left
            }
        } else {
            if (!isTruthy(left)) {
                return left
            }
        }
        return evaluate(logical.right)
    }

    override fun visitWhileStatement(whileLoop: Statement.While) {
        while (isTruthy(evaluate(whileLoop.condition))) {
            execute(whileLoop.body)
        }
    }

    //==== UTILITIES ===================================================================================================
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
