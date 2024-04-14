package dev.edd255.lox

class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = mutableListOf()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1
    private val errorReporter = ErrorReporter()
    private val keywords = Keywords()

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val char: Char = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            ' ', '\r', '\t' -> {
                // Ignore whitespace.
            }
            '\n' -> line++
            '"' -> string()
            'o' -> {
                if (peek() == 'r') {
                    addToken(TokenType.OR)
                } else {
                    identifier()
                }
            }
            else -> {
                if (char.isDigit()) {
                    number()
                } else if (char.isAlpha()) {
                    identifier()
                } else {
                    errorReporter.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun identifier() {
        while (peek().isAlphanumeric()) advance()
        val text: String = source.substring(start, current)
        val type: TokenType = keywords.getKeywordType(text) ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (peek().isDigit()) advance()
        if (peek() == '.' && peekNext().isDigit()) {
            advance()
            while (peek().isDigit()) advance()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            errorReporter.error(line, "Unterminated string.")
            return
        }
        advance()
        val value: String = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun match(c: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != c) return false
        current++
        return true
    }

    private fun advance(): Char = source[current++]

    private fun addToken(type: TokenType): Unit = addToken(type, null)

    private fun addToken(type: TokenType, literal: Any?) {
        val text: String = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun isAtEnd() = current >= source.length
}

private fun Char.isAlpha(): Boolean {
    return this.isLetter() || this == '_'
}

private fun Char.isAlphanumeric(): Boolean {
    return this.isAlpha() || this.isDigit()
}
