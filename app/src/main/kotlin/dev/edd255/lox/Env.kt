package dev.edd255.lox

import java.util.HashMap

class Env(private val enclosing: Env? = null) {
    private val values: MutableMap<String, Any> = HashMap()

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }
        if (enclosing != null) { return enclosing.get(name) }
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun assign(name: Token, value: Any) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }
        enclosing?.assign(name, value)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }
}
