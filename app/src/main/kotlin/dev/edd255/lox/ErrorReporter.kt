package dev.edd255.lox

object ErrorReporter {
    var hadError: Boolean = false
    var hadRuntimeError: Boolean = false

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, "at end", message)
        } else {
            report(token.line, "at '${token.lexeme}'", message)
        }
    }

    fun error(line: Int, message: String) = report(line, "at end", message)

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message")
        hadError = true
    }

    fun runtimeError(error: RuntimeError) {
        println("${error.message}\n[line ${error.token.line}]")
        hadRuntimeError = true
    }
}
