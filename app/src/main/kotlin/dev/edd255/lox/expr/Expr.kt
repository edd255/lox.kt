package dev.edd255.lox.expr

import dev.edd255.lox.Token

abstract class Expr {
    abstract fun <T> accept(visitor: ExprVisitor<T>): T
}

class Assign(val name: Token, val value: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitAssignExpr(this)
}

class Binary(val left: Expr, val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitBinaryExpr(this)
}

class Grouping(val expr: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitGroupingExpr(this)
}

class Literal(val value: Any?) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitLiteralExpr(this)
}

class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitLogicalExpr(this)
}

class Unary(val op: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitUnaryExpr(this)
}

class Variable(val name: Token) : Expr() {
    override fun <T> accept(visitor: ExprVisitor<T>): T = visitor.visitVariableExpr(this)
}

interface ExprVisitor<T> {
    fun visitAssignExpr(expr: Assign): T
    fun visitBinaryExpr(expr: Binary): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitLogicalExpr(expr: Logical): T
    fun visitUnaryExpr(expr: Unary): T
    fun visitVariableExpr(expr: Variable): T
}
