package dev.edd255.lox

import java.util.HashMap

class Environment(internal val enclosing: Environment? = null) {
    private val values: MutableMap<String, Any?> = HashMap()

    fun get(name: Token): Any? {
        return when {
            values.containsKey(name.lexeme) -> values[name.lexeme]
            enclosing != null -> enclosing.get(name)
            else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
        }
    }

    fun getAt(distance: Int, name: String): Any? = ancestor(distance).values[name]

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    private fun ancestor(distance: Int): Environment {
        var environment = this
        for (i in 0 until distance) {
            environment = environment.enclosing!!
        }
        return environment
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        return when {
            values.containsKey(name.lexeme) -> values[name.lexeme] = value
            enclosing != null -> enclosing.assign(name, value)
            else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
        }
    }
}
