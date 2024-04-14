package dev.edd255.lox

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0
    private var errorReporter = ErrorReporter()

    companion object {
        class ParseError : RuntimeException()
    }

    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    private fun expression(): Expression = assignment()

    private fun declaration(): Statement {
        return try {
            when {
                match(TokenType.VAR) -> varDeclaration()
                else -> statement()
            }
        } catch (error: ParseError) {
            synchronize()
            return Statement.ExpressionStatement(Expression.Literal(null))
        }
    }

    private fun statement(): Statement {
        return when {
            match(TokenType.FOR) -> forStatement()
            match(TokenType.IF) -> ifStatement()
            match(TokenType.PRINT) -> printStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.LEFT_BRACE) -> Statement.Block(block())
            else -> expressionStatement()
        }
    }

    private fun forStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer = when {
            match(TokenType.SEMICOLON) -> null
            match(TokenType.VAR) -> varDeclaration()
            else -> expressionStatement()
        }
        val condition = if (!check(TokenType.SEMICOLON)) { expression() } else { Expression.Literal(true) }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")
        val increment = if(!check(TokenType.RIGHT_PAREN)) { expression() } else { null }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")
        var body = statement()
        if (increment != null) {
            body = Statement.Block(listOf(body, Statement.ExpressionStatement(increment)))
        }
        body = Statement.While(condition, body)
        return if (initializer != null) Statement.Block(listOf(initializer, body)) else body
    }

    private fun whileStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after 'condition'.")
        val body = statement()
        return Statement.While(condition, body)
    }

    private fun ifStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        var elseBranch: Statement? = null
        if (match(TokenType.ELSE)) {
            elseBranch = statement()
        }
        return Statement.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Statement {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Statement.Print(value)
    }

    private fun varDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Statement.Variable(name, initializer)
    }

    private fun expressionStatement(): Statement {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Statement.ExpressionStatement(expr)
    }

    private fun block(): List<Statement> {
        val statements = ArrayList<Statement>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun assignment(): Expression {
        val expr = or()
        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expression.Variable) {
                val name = expr.name
                return Expression.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun or(): Expression {
        var expr = and()
        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expression.Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expression {
        var expr = equality()
        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expression.Logical(expr, operator, right)
        }
        return expr
    }

    private fun equality(): Expression {
        var expr = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expression.Binary(expr, operator, right)
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

    private fun comparison(): Expression {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expression.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expression {
        var expr = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expression.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expression {
        var expr = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expression.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expression.Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expression {
        if (match(TokenType.FALSE)) return Expression.Literal(false)
        if (match(TokenType.TRUE)) return Expression.Literal(true)
        if (match(TokenType.NIL)) return Expression.Literal(null)
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expression.Literal(previous().literal)
        }
        if (match(TokenType.IDENTIFIER)) {
            return Expression.Variable(previous())
        }
        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expression.Grouping(expr)
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
