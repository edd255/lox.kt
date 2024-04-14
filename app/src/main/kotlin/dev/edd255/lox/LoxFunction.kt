package dev.edd255.lox

class LoxFunction(private val declaration: Statement.Function) : LoxCallable {
    private var closure: Environment? = null

    constructor(declaration: Statement.Function, closure: Environment?) : this(declaration) {
        this.closure = closure
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for ((index, parameter) in declaration.parameters.withIndex()) {
            environment.define(parameter.lexeme, arguments[index])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun arity(): Int = declaration.parameters.size

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}