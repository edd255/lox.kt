package dev.edd255.lox

import dev.edd255.lox.expr.Binary
import dev.edd255.lox.expr.AstPrinter
import dev.edd255.lox.expr.Unary
import dev.edd255.lox.expr.Literal
import dev.edd255.lox.expr.Grouping
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val lox = Lox()

    val expr = Binary(
        Unary(
            Token(
                TokenType.MINUS,
                "-",
                null,
                1
            ),
            Literal(123)
        ),
        Token(
            TokenType.STAR,
            "*",
            null,
            1
        ),
        Grouping(
            Literal(45.67)
        )
    )
    println(AstPrinter().print(expr))
    when (args.size) {
        0 -> lox.runPrompt()
        1 -> lox.runFile(args[0])
        else -> {
            println("Usage: lox [script]")
            exitProcess(64)
        }
    }
}