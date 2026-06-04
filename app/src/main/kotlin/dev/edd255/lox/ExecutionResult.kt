package dev.edd255.lox

data class ExecutionResult(
    val hadCompileError: Boolean,
    val hadRuntimeError: Boolean,
)
