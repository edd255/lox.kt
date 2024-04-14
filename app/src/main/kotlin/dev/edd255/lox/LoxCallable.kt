package dev.edd255.lox

interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
    fun arity(): Int
}