package dev.edd255.lox.expr

import dev.edd255.lox.Token

abstract class Stmt {
    abstract fun <T> accept(visitor: StmtVisitor<T>): T
}

class Block(val stmts: List<Stmt>) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitBlockStmt(this)
}

class ExprStmt(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitExprStmt(this)
}

class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitIfStmt(this)
}

class Print(val expr: Expr) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitPrintStmt(this)
}

class Var(val name: Token, val initializer: Expr?) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitVarStmt(this)
}

class While(val condition: Expr, val body: Stmt) : Stmt() {
    override fun <T> accept(visitor: StmtVisitor<T>): T = visitor.visitWhileStmt(this)
}

interface StmtVisitor<T> {
    fun visitBlockStmt(stmt: Block): T
    fun visitExprStmt(stmt: ExprStmt): T
    fun visitIfStmt(stmt: If): T
    fun visitPrintStmt(stmt: Print): T
    fun visitVarStmt(stmt: Var): T
    fun visitWhileStmt(stmt: While): T
}
