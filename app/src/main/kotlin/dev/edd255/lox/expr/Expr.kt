package dev.edd255.lox.expr

import dev.edd255.lox.Token

abstract class Expr {
    abstract fun <T> accept(visitor: ExprVisitor<T>): T
}

class Assign(private val name: Token, private val value: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitAssignExpr(this)
    fun getName(): Token = name
    fun getValue(): Expr = value
}

class Binary(private val left: Expr, private val op: Token, private val right: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitBinaryExpr(this)
    fun getLeft(): Expr = left
    fun getOp(): Token = op
    fun getRight(): Expr = right
}

class Grouping(private val expr: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitGroupingExpr(this)
    fun getExpr(): Expr = expr
}

class Literal(private val value: Any?) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitLiteralExpr(this)
    fun getValue(): Any? = value
}

class Unary(private val op: Token, private val right: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitUnaryExpr(this)
    fun getOp(): Token = op
    fun getRight(): Expr = right
}

class Variable(private val name: Token) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitVariableExpr(this)
    fun getName(): Token = name
}

interface ExprVisitor<T> {
    fun visitAssignExpr(expr: Assign): T
    fun visitBinaryExpr(expr: Binary): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitUnaryExpr(expr: Unary): T
    fun visitVariableExpr(expr: Variable): T
}
