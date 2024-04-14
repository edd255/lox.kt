package dev.edd255.lox

import dev.edd255.lox.expr.*

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0
    private var errorReporter = ErrorReporter()

    companion object {
        class ParseError : RuntimeException()
    }

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    private fun expression(): Expr = assignment()

    private fun declaration(): Stmt {
        return try {
            when {
                match(TokenType.VAR) -> varDeclaration()
                else -> statement()
            }
        } catch (error: ParseError) {
            synchronize()
            return ExprStmt(Literal(null))
        }
    }

    private fun statement(): Stmt {
        return when {
            match(TokenType.FOR) -> forStatement()
            match(TokenType.IF) -> ifStatement()
            match(TokenType.PRINT) -> printStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.LEFT_BRACE) -> Block(block())
            else -> expressionStatement()
        }
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer = when {
            match(TokenType.SEMICOLON) -> null
            match(TokenType.VAR) -> varDeclaration()
            else -> expressionStatement()
        }
        val condition = if (!check(TokenType.SEMICOLON)) { expression() } else { Literal(true) }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")
        val increment = if(!check(TokenType.RIGHT_PAREN)) { expression() } else { null }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")
        var body = statement()
        if (increment != null) {
            body = Block(listOf(body, ExprStmt(increment)))
        }
        body = While(condition, body)
        return if (initializer != null) Block(listOf(initializer, body)) else body
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after 'condition'.")
        val body = statement()
        return While(condition, body)
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(TokenType.ELSE)) {
            elseBranch = statement()
        }
        return If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Print(value)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Var(name, initializer)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return ExprStmt(expr)
    }

    private fun block(): List<Stmt> {
        val stmts = ArrayList<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return stmts
    }

    private fun assignment(): Expr {
        val expr = or()
        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Variable) {
                val name = expr.name
                return Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()
        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()
        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Logical(expr, operator, right)
        }
        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Literal(false)
        if (match(TokenType.TRUE)) return Literal(true)
        if (match(TokenType.NIL)) return Literal(null)
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Literal(previous().literal)
        }
        if (match(TokenType.IDENTIFIER)) {
            return Variable(previous())
        }
        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Grouping(expr)
        }
        throw error(peek(), "Expect expression.")
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) {
            return advance()
        }
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        errorReporter.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return
            when (peek().type) {
                TokenType.CLASS,
                TokenType.FN,
                TokenType.VAR,
                TokenType.FOR,
                TokenType.IF,
                TokenType.WHILE,
                TokenType.PRINT,
                TokenType.RETURN,
                -> return
                else -> {}
            }
            advance()
        }
    }
}
