package org.edd255.lox.expr

import org.edd255.lox.Token

abstract class Expr {
    abstract fun <T> accept(visitor: Visitor<T>): T
}

class Binary(val left: Expr, val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitBinaryExpr(this)
    }
}


class Grouping(val expr: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitGroupingExpr(this)
    }
}


class Literal(val value: Any) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitLiteralExpr(this)
    }
}


class Unary(val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visitUnaryExpr(this)
    }
}


interface Visitor<T> {
    fun visitBinaryExpr(expr: Binary): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitUnaryExpr(expr: Unary): T
}
