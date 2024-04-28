package dev.edd255.lox

class AstPrinter : Expression.Visitor<String>, Statement.Visitor<String> {
    fun print(expression: Expression): String = expression.accept(this)

    fun print(statement: Statement): String = statement.accept(this)

    override fun visitBinaryExpression(binary: Expression.Binary): String = parenthesizeExpression(
        binary.operator.lexeme,
        binary.left,
        binary.right,
    )

    override fun visitCallExpression(call: Expression.Call): String =
        parenthesizeExpression("call", call.callee, *call.arguments.toTypedArray())

    override fun visitGetExpression(get: Expression.Get): String =
        parenthesizeExpression("get '${get.name.lexeme}'", get.obj)

    override fun visitGroupingExpression(grouping: Expression.Grouping): String =
        parenthesizeExpression("group", grouping.expression)

    override fun visitLiteralExpression(literal: Expression.Literal): String = literal.value.toString()

    override fun visitLogicalExpression(logical: Expression.Logical): String =
        parenthesizeExpression(logical.operator.lexeme, logical.left, logical.right)

    override fun visitSetExpression(set: Expression.Set): String =
        parenthesizeExpression("set '${set.name.lexeme}'", set.obj, set.value)

    override fun visitSuperExpression(superExpression: Expression.Super): String =
        parenthesizeExpression("super '${superExpression.method.lexeme}'")

    override fun visitThisExpression(thisStatement: Expression.This): String = "this"

    override fun visitUnaryExpression(unary: Expression.Unary): String =
        parenthesizeExpression(unary.operator.lexeme, unary.right)

    override fun visitVariableExpression(variable: Expression.Variable): String =
        parenthesizeExpression("var '${variable.name.lexeme}'")

    override fun visitAssignExpression(assign: Expression.Assign): String =
        parenthesizeExpression("assign '${assign.name.lexeme}'", assign.value)

    override fun visitBlockStatement(block: Statement.Block): String {
        val builder = java.lang.StringBuilder()
        builder.append("(block ")
        for (statement in block.statements) {
            builder.append(statement.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    override fun visitClassStatement(classStatement: Statement.Class): String {
        val builder = java.lang.StringBuilder()
        builder.append("(class " + classStatement.name.lexeme)
        if (classStatement.superclass != null) {
            builder.append(" < " + print(classStatement.superclass))
        }
        for (method in classStatement.methods) {
            builder.append(" " + print(method))
        }
        builder.append(")")
        return builder.toString()
    }

    override fun visitExpressionStatement(expressionStatement: Statement.ExpressionStatement): String {
        return parenthesizeExpression(";", expressionStatement.expression)
    }

    override fun visitFunctionStatement(function: Statement.Function): String {
        val builder = java.lang.StringBuilder()
        builder.append("(fn " + function.name.lexeme + "(")
        for (param in function.parameters) {
            if (param != function.parameters[0]) builder.append(" ")
            builder.append(param.lexeme)
        }
        builder.append(") ")
        for (body in function.body) {
            builder.append(body.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    override fun visitIfStatement(ifQuery: Statement.If): String {
        if (ifQuery.elseBranch == null) {
            return parenthesizeStatement("if", ifQuery.condition, ifQuery.thenBranch)
        }

        return parenthesizeStatement(
            "if-else", ifQuery.condition, ifQuery.thenBranch,
            ifQuery.elseBranch
        )
    }

    override fun visitPrintStatement(print: Statement.Print): String {
        return parenthesizeExpression("print", print.expression)
    }

    override fun visitReturnStatement(returnStatement: Statement.Return): String {
        if (returnStatement.value == null) return "(return)"
        return parenthesizeExpression("return", returnStatement.value)
    }

    override fun visitVariableStatement(variable: Statement.Variable): String {
        if (variable.initializer == null) {
            return parenthesizeStatement("var", variable.name)
        }
        return parenthesizeStatement("var", variable.name, "=", variable.initializer)
    }

    override fun visitWhileStatement(whileLoop: Statement.While): String {
        return parenthesizeStatement("while", whileLoop.condition, whileLoop.body)
    }

    private fun parenthesizeExpression(name: String, vararg expressions: Expression): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        for (expr in expressions) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    private fun parenthesizeStatement(name: String, vararg statements: Any): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        transform(builder, statements)
        builder.append(")")
        println(builder.toString())
        return builder.toString()
    }

    private fun transform(builder: StringBuilder, vararg statements: Any) {
        for (statement in statements) {
            builder.append(" ")
            when (statement) {
                is Expression -> builder.append(statement.accept(this))
                is Statement -> builder.append(statement.accept(this))
                is Token -> builder.append(statement.lexeme)
                is List<*> -> transform(builder, statements)
                else -> builder.append(statement)
            }
        }
    }
}
