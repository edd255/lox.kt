package dev.edd255.lox

abstract class Statement {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Block(val statements: List<Statement>) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBlockStatement(this)
    }

    class Class(val name: Token, val methods: List<Statement.Function>) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitClassStatement(this)
    }

    class ExpressionStatement(val expression: Expression) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitExpressionStatement(this)
    }

    class Function(val name: Token, val parameters: List<Token>, val body: List<Statement>) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitFunctionStatement(this)
    }

    class If(val condition: Expression, val thenBranch: Statement, val elseBranch: Statement?) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitIfStatement(this)
    }

    class Print(val expression: Expression) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitPrintStatement(this)
    }

    class Return(val keyword: Token, val value: Expression?) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitReturnStatement(this)
    }

    class Variable(val name: Token, val initializer: Expression?) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVariableStatement(this)
    }

    class While(val condition: Expression, val body: Statement) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitWhileStatement(this)
    }

    interface Visitor<T> {
        fun visitBlockStatement(block: Block): T
        fun visitClassStatement(classStatement: Statement.Class): T
        fun visitExpressionStatement(expressionStatement: ExpressionStatement): T
        fun visitFunctionStatement(function: Function): T
        fun visitIfStatement(ifQuery: If): T
        fun visitPrintStatement(print: Print): T
        fun visitReturnStatement(returnStatement: Return): T
        fun visitVariableStatement(variable: Variable): T
        fun visitWhileStatement(whileLoop: While): T
    }
}