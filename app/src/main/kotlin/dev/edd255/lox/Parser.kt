package dev.edd255.lox

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

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

    //==== STATEMENTS ==================================================================================================
    private fun statement(): Statement {
        return when {
            match(TokenType.FOR) -> forStatement()
            match(TokenType.IF) -> ifStatement()
            match(TokenType.PRINT) -> printStatement()
            match(TokenType.RETURN) -> returnStatement()
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

    private fun returnStatement(): Statement {
        val keyword = previous()
        val value = if (!check(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return Statement.Return(keyword, value)
    }

    private fun whileStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after 'condition'.")
        val body = statement()
        return Statement.While(condition, body)
    }

    private fun block(): List<Statement> {
        val statements = ArrayList<Statement>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun declaration(): Statement {
        return try {
            when {
                match(TokenType.CLASS) -> classDeclaration()
                match(TokenType.FN) -> function("function")
                match(TokenType.VAR) -> varDeclaration()
                else -> statement()
            }
        } catch (error: ParseError) {
            synchronize()
            return Statement.ExpressionStatement(Expression.Literal(null))
        }
    }

    private fun classDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect class name.")
        var superclass: Expression.Variable? = null
        if (match(TokenType.COLON)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.")
            superclass = Expression.Variable(previous())
        }
        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.")
        val methods = mutableListOf<Statement.Function>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            match(TokenType.FN)
            methods.add(function("method"))
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.")
        return Statement.Class(name, superclass, methods)
    }

    private fun function(kind: String): Statement.Function {
        val name = consume(TokenType.IDENTIFIER, "Expect $kind name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.")
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Statement.Function(name, parameters, body)
    }

    private fun varDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Statement.Variable(name, initializer)
    }

    private fun expressionStatement(): Statement {
        val expression = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Statement.ExpressionStatement(expression)
    }

    //==== EXPRESSIONS =================================================================================================
    private fun expression(): Expression = assignment()

    private fun assignment(): Expression {
        val expression = or()
        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            when (expression) {
                is Expression.Variable -> return Expression.Assign(expression.name, value)
                is Expression.Get -> return Expression.Set(expression.obj, expression.name, value)
                else -> error(equals, "Invalid assignment returns.")
            }
        }
        return expression
    }

    private fun or(): Expression {
        var expression = and()
        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expression = Expression.Logical(expression, operator, right)
        }
        return expression
    }

    private fun and(): Expression {
        var expression = equality()
        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expression = Expression.Logical(expression, operator, right)
        }
        return expression
    }

    private fun equality(): Expression {
        var expression = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expression = Expression.Binary(expression, operator, right)
        }
        return expression
    }

    private fun comparison(): Expression {
        var expression = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expression = Expression.Binary(expression, operator, right)
        }
        return expression
    }

    private fun term(): Expression {
        var expression = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expression = Expression.Binary(expression, operator, right)
        }
        return expression
    }

    private fun factor(): Expression {
        var expression = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expression = Expression.Binary(expression, operator, right)
        }
        return expression
    }

    private fun unary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expression.Unary(operator, right)
        }
        return call()
    }

    private fun call(): Expression {
        var expression = primary()
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expression = finishCall(expression)
            } else if (match(TokenType.DOT)) {
                val name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.")
                expression = Expression.Get(expression, name)
            } else {
                break
            }
        }
        return expression
    }

    private fun finishCall(callee: Expression): Expression {
        val arguments = mutableListOf<Expression>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Cannot have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(TokenType.COMMA))
        }
        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
        return Expression.Call(callee, paren, arguments)
    }

    private fun primary(): Expression {
        if (match(TokenType.FALSE)) return Expression.Literal(false)
        if (match(TokenType.TRUE)) return Expression.Literal(true)
        if (match(TokenType.NIL)) return Expression.Literal(null)
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expression.Literal(previous().literal)
        }
        if (match(TokenType.SUPER)) {
            val keyword = previous()
            consume(TokenType.DOT, "Expect '.' after 'super'.")
            val method = consume(TokenType.IDENTIFIER, "Expect superclass method name.")
            return Expression.Super(keyword, method)
        }
        if (match(TokenType.THIS)) {
            return Expression.This(previous())
        }
        if (match(TokenType.IDENTIFIER)) {
            return Expression.Variable(previous())
        }
        if (match(TokenType.LEFT_PAREN)) {
            val expression = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expression.Grouping(expression)
        }
        throw error(peek(), "Expect expression.")
    }

    //==== UTILITIES ===================================================================================================
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

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) {
            return advance()
        }
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        ErrorReporter.error(token, message)
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
