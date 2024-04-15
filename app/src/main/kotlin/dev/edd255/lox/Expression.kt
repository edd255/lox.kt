package dev.edd255.lox

abstract class Expression {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Assign(val name: Token, val value: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAssignExpression(this)
    }

    class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBinaryExpression(this)
    }

    class Call(val callee: Expression, val paren: Token, val arguments: List<Expression>) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCallExpression(this)
    }

    class Get(val obj: Expression, val name: Token) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGetExpression(this)
    }

    class Grouping(val expression: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGroupingExpression(this)
    }

    class Literal(val value: Any?) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLiteralExpression(this)
    }

    class Logical(val left: Expression, val operator: Token, val right: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLogicalExpression(this)
    }

    class Set(val obj: Expression, val name: Token, val set: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitSetExpression(this)
    }

    class This(val keyword: Token) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitThisExpression(this)
    }

    class Unary(val operator: Token, val right: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnaryExpression(this)
    }

    class Variable(val name: Token) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVariableExpression(this)
    }

    interface Visitor<T> {
        fun visitAssignExpression(assign: Assign): T
        fun visitBinaryExpression(binary: Binary): T
        fun visitCallExpression(call: Call): T
        fun visitGetExpression(get: Get): T
        fun visitGroupingExpression(grouping: Grouping): T
        fun visitLiteralExpression(literal: Literal): T
        fun visitLogicalExpression(logical: Logical): T
        fun visitSetExpression(set: Set): T
        fun visitThisExpression(thisStatement: This): T
        fun visitUnaryExpression(unary: Unary): T
        fun visitVariableExpression(variable: Variable): T
    }
}