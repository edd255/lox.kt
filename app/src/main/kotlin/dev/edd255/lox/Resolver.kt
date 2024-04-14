package dev.edd255.lox

fun <T> ArrayDeque<T>.push(element: T) = addLast(element)

fun <T> ArrayDeque<T>.pop() = removeLastOrNull()

class Resolver(private val interpreter: Interpreter) : Expression.Visitor<Unit>, Statement.Visitor<Unit> {
    private val scopes = ArrayDeque<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    //==== SCOPES =====================================================================================================
    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    //==== EXPRESSIONS ================================================================================================
    private fun resolve(expression: Expression) = expression.accept(this)

    override fun visitAssignExpression(assign: Expression.Assign) {
        resolve(assign.value)
        resolveLocal(assign, assign.name)
    }

    override fun visitBinaryExpression(binary: Expression.Binary) {
        resolve(binary.left)
        resolve(binary.right)
    }

    override fun visitCallExpression(call: Expression.Call) {
        resolve(call.callee)
        call.arguments.forEach { resolve(it) }
    }

    override fun visitGroupingExpression(grouping: Expression.Grouping) {
        resolve(grouping.expression)
    }

    override fun visitLiteralExpression(literal: Expression.Literal) {
        // Do nothing
    }

    override fun visitLogicalExpression(logical: Expression.Logical) {
        resolve(logical.left)
        resolve(logical.right)
    }

    override fun visitUnaryExpression(unary: Expression.Unary) {
        resolve(unary.right)
    }

    override fun visitVariableExpression(variable: Expression.Variable) {
        if (scopes.isNotEmpty() && scopes.first()[variable.name.lexeme] == false) {
            ErrorReporter.error(variable.name, "Cannot read local variable in its own initializer.")
        }
        resolveLocal(variable, variable.name)
    }

    //==== STATEMENTS =================================================================================================
    fun resolve(statements: List<Statement>) = statements.forEach { resolve(it) }

    fun resolve(statement: Statement) = statement.accept(this)

    private fun resolveLocal(expression: Expression, name: Token) {
        for (i in scopes.indices) {
            if (name.lexeme in scopes.elementAt(i)) {
                interpreter.resolve(expression, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.first()
        if (name.lexeme in scope) {
            ErrorReporter.error(name, "Variable with this name already declared in this scope.")
        }
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.first()[name.lexeme] = true
    }

    override fun visitBlockStatement(block: Statement.Block) {
        beginScope()
        resolve(block.statements)
        endScope()
    }

    override fun visitExpressionStatement(expressionStatement: Statement.ExpressionStatement) {
        resolve(expressionStatement.expression)
    }

    override fun visitFunctionStatement(function: Statement.Function) {
        declare(function.name)
        define(function.name)
        resolveFunction(function, FunctionType.FUNCTION)
    }

    private fun resolveFunction(function: Statement.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        function.parameters.forEach {
            declare(it)
            define(it)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    override fun visitIfStatement(ifQuery: Statement.If) {
        resolve(ifQuery.condition)
        resolve(ifQuery.thenBranch)
        if (ifQuery.elseBranch != null) resolve(ifQuery.elseBranch)
    }

    override fun visitPrintStatement(print: Statement.Print) {
        resolve(print.expression)
    }

    override fun visitReturnStatement(returnStatement: Statement.Return) {
        if (currentFunction == FunctionType.NONE) {
            ErrorReporter.error(returnStatement.keyword, "Cannot return from top-level code.")
        }
        if (returnStatement.value != null) resolve(returnStatement.value)
    }

    override fun visitVariableStatement(variable: Statement.Variable) {
        declare(variable.name)
        if (variable.initializer != null) {
            resolve(variable.initializer)
        }
        define(variable.name)
    }

    override fun visitWhileStatement(whileLoop: Statement.While) {
        resolve(whileLoop.condition)
        resolve(whileLoop.body)
    }

}