package dev.edd255.lox

class LoxClass(val name: String, private val methods: Map<String, LoxFunction>) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)
        return initializer
    }

    override fun arity(): Int {
        val initializer = findMethod("init")
        return initializer?.arity() ?: 0
    }

    override fun toString(): String = name

    fun findMethod(name: String): LoxFunction? {
        return methods[name]
    }
}