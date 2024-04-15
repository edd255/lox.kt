package dev.edd255.lox

class LoxClass(val name: String, private val methods: Map<String, LoxFunction>) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        findMethod("init")?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun arity(): Int = findMethod("init")?.arity() ?: 0

    override fun toString(): String = name

    fun findMethod(name: String): LoxFunction? {
        return methods[name]
    }
}