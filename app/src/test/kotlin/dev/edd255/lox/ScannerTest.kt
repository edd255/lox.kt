package dev.edd255.lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScannerTest {
    @Test
    fun `scans punctuation operators comments and eof`() {
        val result = scanSource("(){},.-+;* : ! != = == < <= > >= / // comment\n")

        assertFalse(result.hadError, result.stderr)
        assertEquals(
            listOf(
                TokenType.LEFT_PAREN,
                TokenType.RIGHT_PAREN,
                TokenType.LEFT_BRACE,
                TokenType.RIGHT_BRACE,
                TokenType.COMMA,
                TokenType.DOT,
                TokenType.MINUS,
                TokenType.PLUS,
                TokenType.SEMICOLON,
                TokenType.STAR,
                TokenType.COLON,
                TokenType.BANG,
                TokenType.BANG_EQUAL,
                TokenType.EQUAL,
                TokenType.EQUAL_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL,
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.SLASH,
                TokenType.EOF,
            ),
            result.tokens.map { it.type },
        )
        assertEquals(2, result.tokens.last().line)
    }

    @Test
    fun `scans literals identifiers and keywords`() {
        val result = scanSource(
            """
            class Foo: Bar {
                fn method(identifier_1) {
                    var text = "lox";
                    var number = 123.45;
                    print true and false or nil;
                    return this;
                }
            }
            for if else super while
            """.trimIndent()
        )

        assertFalse(result.hadError, result.stderr)
        assertEquals(TokenType.CLASS, result.tokens[0].type)
        assertEquals("Foo", result.tokens[1].lexeme)
        assertEquals(TokenType.COLON, result.tokens[2].type)
        assertEquals("lox", result.tokens.first { it.type == TokenType.STRING }.literal)
        assertEquals(123.45, result.tokens.first { it.type == TokenType.NUMBER }.literal)
        assertTrue(result.tokens.any { it.type == TokenType.THIS })
        assertTrue(result.tokens.any { it.type == TokenType.SUPER })
        assertTrue(result.tokens.any { it.type == TokenType.WHILE })
        assertTrue(result.tokens.any { it.lexeme == "identifier_1" && it.type == TokenType.IDENTIFIER })
    }

    @Test
    fun `reports lexical errors and still emits eof`() {
        val result = scanSource("@")

        assertTrue(result.hadError)
        assertTrue(result.stderr.contains("Unexpected character."), result.stderr)
        assertEquals(TokenType.EOF, result.tokens.last().type)
    }
}
