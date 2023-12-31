package dev.edd255.lox

class ErrorReporter {
    private var hadError: Boolean = false
    private var hadRuntimeError: Boolean = false

    fun error(token: Token, message: String) {
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), "at end", message)
        } else {
            report(token.getLine(), "at '$token.lexeme'", message)
        }
    }

    fun error(line: Int, message: String) = report(line, "at end", message)

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message")
        hadError = true
    }

    fun runtimeError(error: RuntimeError) {
        println("${error.message}\n[line ${error.token.getLine()}]")
        hadRuntimeError = true
    }

    fun setHadError(b: Boolean) {
        hadError = b
    }

    fun getHadError(): Boolean = hadError
    fun getHadRuntimeError(): Boolean = hadRuntimeError
}
