package dev.edd255.lox.expr

import dev.edd255.lox.Token

abstract class Stmt {
    abstract fun <T> accept(visitor: Visitor<T>): T
}

class Expression(private val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitExpressionStmt(this)
    fun getExpr(): Expr = expr
}

class Print(private val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitPrintStmt(this)
    fun getExpr(): Expr = expr
}

interface StmtVisitor<T> {
    fun visitExpressionStmt(stmt: Expression): T
    fun visitPrintStmt(stmt: Print): T
}
