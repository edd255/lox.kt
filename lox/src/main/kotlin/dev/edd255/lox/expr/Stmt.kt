package dev.edd255.lox.expr

import dev.edd255.lox.Token

abstract class Stmt {
    abstract fun <T> accept(visitor: StmtVisitor<T>): T
}

class Block(private val stmts: List<Stmt>) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitBlockStmt(this)
    fun getStmts(): List<Stmt> = stmts
}

class ExprStmt(private val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitExprStmt(this)
    fun getExpr(): Expr = expr
}

class Print(private val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitPrintStmt(this)
    fun getExpr(): Expr = expr
}

class Var(private val name: Token, private val initializer: Expr?) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitVarStmt(this)
    fun getName(): Token = name
    fun getInitializer(): Expr? = initializer
}

interface StmtVisitor<T> {
    fun visitBlockStmt(stmt: Block): T
    fun visitExprStmt(stmt: ExprStmt): T
    fun visitPrintStmt(stmt: Print): T
    fun visitVarStmt(stmt: Var): T
}
