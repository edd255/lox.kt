package dev.edd255.lox

class LoxFunction(private val declaration: Statement.Function) : LoxCallable {
    private var closure: Environment? = null
    private var isInitializer = false

    constructor(declaration: Statement.Function, closure: Environment?, isInitializer: Boolean) : this(declaration) {
        this.closure = closure
        this.isInitializer = isInitializer
    }

    override fun arity(): Int = declaration.parameters.size

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for ((index, parameter) in declaration.parameters.withIndex()) {
            environment.define(parameter.lexeme, arguments[index])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            if (isInitializer) return closure?.getAt(0, "this")
            return returnValue.value
        }
        if (isInitializer) return closure?.getAt(0, "this")
        return null
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}