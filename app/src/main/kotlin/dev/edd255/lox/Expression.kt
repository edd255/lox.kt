package dev.edd255.lox

abstract class Expression {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Assign(val name: Token, val value: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAssignExpression(this)
    }

    class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBinaryExpression(this)
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

    class Unary(val operator: Token, val right: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnaryExpression(this)
    }

    class Variable(val name: Token) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVariableExpression(this)
    }

    interface Visitor<T> {
        fun visitAssignExpression(expression: Assign): T
        fun visitBinaryExpression(expression: Binary): T
        fun visitGroupingExpression(expression: Grouping): T
        fun visitLiteralExpression(expression: Literal): T
        fun visitLogicalExpression(expression: Logical): T
        fun visitUnaryExpression(expression: Unary): T
        fun visitVariableExpression(expression: Variable): T
    }
}