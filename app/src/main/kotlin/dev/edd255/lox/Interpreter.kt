package dev.edd255.lox

import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

class Interpreter : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    private val globals = Environment()
    private val locals = hashMapOf<Expression, Int>()
    private var environment = globals
    private val getcStream = InputStreamReader(System.`in`, StandardCharsets.UTF_8)

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
        globals.define(
            "getc",
            object : LoxCallable {
                override fun arity(): Int = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    try {
                        val c = getcStream.read()
                        if (c < 0) {
                            return -1.0
                        }
                        return c.toDouble()
                    } catch (error: IOException) {
                        return -1.0
                    }
                }

                override fun toString(): String = "<native fn>"
            }
        )
        globals.define(
            "chr",
            object : LoxCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    return Character.toString((arguments[0] as Double).toInt().toChar())
                }

                override fun toString(): String = "<native fn>"
            }
        )
        globals.define(
            "exit",
            object : LoxCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    exitProcess((arguments[0] as Double).toInt())
                }

                override fun toString(): String = "<native fn>"
            }
        )
        globals.define(
            "print_error",
            object : LoxCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    System.err.println(arguments[0])
                    return Unit
                }

                override fun toString(): String = "<native fn>"
            }
        )
    }

    fun interpret(statements: List<Statement>) {
        try {
            statements.forEach { execute(it) }
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

    private fun checkBinaryExpression(left: Any?, right: Any?, operator: Token) {
        if (left !is Double || right !is Double) throw RuntimeError(operator, "Operands must be numbers.",)
    }

    override fun visitBinaryExpression(binary: Expression.Binary): Any? {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)
        return when (binary.operator.type) {
            TokenType.MINUS -> {
                checkBinaryExpression(left, right, binary.operator)
                (left as Double) - (right as Double)
            }
            TokenType.SLASH -> {
                checkBinaryExpression(left, right, binary.operator)
                (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkBinaryExpression(left, right, binary.operator)
                (left as Double) * (right as Double)
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(binary.operator, "Operands must be two numbers or two strings.")
                }
            }
            TokenType.GREATER -> {
                checkBinaryExpression(left, right, binary.operator)
                (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkBinaryExpression(left, right, binary.operator)
                (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkBinaryExpression(left, right, binary.operator)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkBinaryExpression(left, right, binary.operator)
                (left as Double) <= (right as Double)
            }
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            else -> null
        }
    }

    override fun visitGetExpression(get: Expression.Get): Any? {
        val value = evaluate(get.obj)
        if (value is LoxInstance) {
            return value.get(get.name)
        }
        throw RuntimeError(get.name, "Only instances have properties")
    }

    override fun visitGroupingExpression(grouping: Expression.Grouping): Any? = evaluate(grouping.expression)

    override fun visitLiteralExpression(literal: Expression.Literal): Any? = literal.value

    override fun visitLogicalExpression(logical: Expression.Logical): Any? {
        val left = evaluate(logical.left)
        if (logical.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }
        return evaluate(logical.right)
    }

    override fun visitSetExpression(set: Expression.Set): Any? {
        val obj = evaluate(set.obj)
        if (obj !is LoxInstance) {
            throw RuntimeError(set.name, "Only instances have fields")
        }
        val value = evaluate(set.value)
        obj.set(set.name, value)
        return value
    }

    override fun visitSuperExpression(superExpression: Expression.Super): Any {
        val distance = locals[superExpression]
        val superclass = environment.getAt(distance!!, "super") as LoxClass
        val obj = environment.getAt(distance - 1, "this") as LoxInstance
        val method = superclass.findMethod(superExpression.method.lexeme)
            ?: throw RuntimeError(superExpression.method, "Undefined property '${superExpression.method.lexeme}'")
        return method.bind(obj)
    }

    override fun visitThisExpression(thisStatement: Expression.This): Any? =
        lookUpVariable(thisStatement.keyword, thisStatement)

    override fun visitUnaryExpression(unary: Expression.Unary): Any? {
        val right = evaluate(unary.right)
        return when (unary.operator.type) {
            TokenType.MINUS -> {
                if (right is Double) -right else throw RuntimeError(unary.operator, "Operand must be a number")
            }
            TokenType.BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitVariableExpression(variable: Expression.Variable): Any? = lookUpVariable(variable.name, variable)

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
            statements.forEach { execute(it) }
        } finally {
            this.environment = previous
        }
    }

    override fun visitBlockStatement(block: Statement.Block) {
        executeBlock(block.statements, Environment(environment))
    }

    override fun visitClassStatement(classStatement: Statement.Class) {
        val superclass = if (classStatement.superclass != null) evaluate(classStatement.superclass) else null
        if (classStatement.superclass != null && superclass !is LoxClass) {
            throw RuntimeError(classStatement.superclass.name, "Superclass must be a class")
        }
        environment.define(classStatement.name.lexeme, null)
        if (classStatement.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }
        val methods = hashMapOf<String, LoxFunction>()
        for (method in classStatement.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "init")
            methods[method.name.lexeme] = function
        }
        lateinit var loxClass: LoxClass
        if (superclass != null) {
            loxClass = LoxClass(
                classStatement.name.lexeme,
                superclass as LoxClass,
                methods
            )
            environment = environment.enclosing!!
        } else {
            loxClass = LoxClass(
                classStatement.name.lexeme,
                null,
                methods
            )
        }
        environment.assign(classStatement.name, loxClass)
    }

    override fun visitExpressionStatement(expressionStatement: Statement.ExpressionStatement) {
        evaluate(expressionStatement.expression)
    }

    override fun visitFunctionStatement(function: Statement.Function) {
        val loxFunction = LoxFunction(function, environment, false)
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
        environment.define(variable.name.lexeme, value)
    }

    override fun visitIfStatement(ifQuery: Statement.If) {
        if (isTruthy(evaluate(ifQuery.condition))) {
            execute(ifQuery.thenBranch)
        } else if (ifQuery.elseBranch != null) {
            execute(ifQuery.elseBranch)
        }
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
            else -> true
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
