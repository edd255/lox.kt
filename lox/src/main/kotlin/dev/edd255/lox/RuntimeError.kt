package dev.edd255.lox

class RuntimeError(val token: Token) : RuntimeException() {
    override var message: String = ""

    constructor(token: Token, message: String) : this(token) {
        this.message = message
    }
}
