package dev.edd255.lox.expr

import dev.edd255.lox.Token

abstract class Expr {
    abstract fun <T> accept(visitor: Visitor<T>): T
}

class Binary(private val left: Expr, private val op: Token, private val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBinaryExpr(this)
    fun getLeft(): Expr = left
    fun getOp(): Token = op
    fun getRight(): Expr = right
}

class Grouping(private val expr: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGroupingExpr(this)
    fun getExpr(): Expr = expr
}

class Literal(private val value: Any?) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLiteralExpr(this)
    fun getValue(): Any? = value
}

class Unary(private val op: Token, private val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnaryExpr(this)
    fun getOp(): Token = op
    fun getRight(): Expr = right
}

interface Visitor<T> {
    fun visitBinaryExpr(expr: Binary): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitUnaryExpr(expr: Unary): T
}
