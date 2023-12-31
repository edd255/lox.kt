package dev.edd255.lox

class Token(private val type: TokenType, private val lexeme: String, private val literal: Any?, private val line: Int) {
    override fun toString(): String = "$type $lexeme $literal $line"
    fun getType(): TokenType = type

    fun getLexeme(): String = lexeme

    fun getLiteral(): Any? = literal

    fun getLine(): Int = line
}
