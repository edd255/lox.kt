package dev.edd255.lox

class Keywords {
    private val keywords: MutableMap<String, TokenType> = HashMap()

    init {
        keywords["and"] = TokenType.AND
        keywords["class"] = TokenType.CLASS
        keywords["else"] = TokenType.ELSE
        keywords["false"] = TokenType.FALSE
        keywords["for"] = TokenType.FOR
        keywords["fn"] = TokenType.FN
        keywords["if"] = TokenType.IF
        keywords["nil"] = TokenType.NIL
        keywords["or"] = TokenType.OR
        keywords["print"] = TokenType.PRINT
        keywords["return"] = TokenType.RETURN
        keywords["super"] = TokenType.SUPER
        keywords["this"] = TokenType.THIS
        keywords["true"] = TokenType.TRUE
        keywords["var"] = TokenType.VAR
        keywords["while"] = TokenType.WHILE
    }

    fun getKeywordType(text: String): TokenType? = keywords[text]
}
