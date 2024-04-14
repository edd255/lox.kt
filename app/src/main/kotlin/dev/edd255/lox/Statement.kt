package dev.edd255.lox

abstract class Statement {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Block(val statements: List<Statement>) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBlockStatement(this)
    }

    class ExpressionStatement(val expression: Expression) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitExpressionStatement(this)
    }

    class If(val condition: Expression, val thenBranch: Statement, val elseBranch: Statement?) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitIfStatement(this)
    }

    class Print(val expression: Expression) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitPrintStatement(this)
    }

    class Variable(val name: Token, val initializer: Expression?) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVarStatement(this)
    }

    class While(val condition: Expression, val body: Statement) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitWhileStatement(this)
    }

    interface Visitor<T> {
        fun visitBlockStatement(statement: Block): T
        fun visitExpressionStatement(statement: ExpressionStatement): T
        fun visitIfStatement(statement: If): T
        fun visitPrintStatement(statement: Print): T
        fun visitVarStatement(statement: Variable): T
        fun visitWhileStatement(statement: While): T
    }
}