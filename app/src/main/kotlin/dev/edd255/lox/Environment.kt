package dev.edd255.lox

import java.util.HashMap

class Environment(private val enclosing: Environment? = null) {
    private val values: MutableMap<String, Any> = HashMap()

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }
        if (enclosing != null) { return enclosing.get(name) }
        throw RuntimeError(name, "Tried to access undefined variable '${name.lexeme}'.")
    }

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun assign(name: Token, value: Any) {
        return when {
            values.containsKey(name.lexeme) -> {
                values[name.lexeme] = value
            }
            enclosing != null -> {
                enclosing.assign(name, value)
            }
            else -> {
                throw RuntimeError(name, "Tried to assign undefined variable '${name.lexeme}'.")
            }
        }
    }
}
