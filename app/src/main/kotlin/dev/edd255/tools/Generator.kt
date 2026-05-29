package dev.edd255.tools

import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output_dir>")
        exitProcess(64)
    }
    val outputDir = args[0]
    val generator = Generator()
    generator.defineAst(
        outputDir,
        "Expression",
        listOf(
            "Assign   : name: Token, value: Expression",
            "Binary   : left: Expression, operator: Token, right: Expression",
            "Call     : callee: Expression, paren: Token, arguments: List<Expression>",
            "Get      : obj: Expression, name: Token",
            "Grouping : expression: Expression",
            "Literal  : value: Any?",
            "Logical  : left: Expression, operator: Token, right: Expression",
            "Set      : obj: Expression, name: Token, value: Expression",
            "Super    : keyword: Token, method: Token",
            "This     : keyword: Token",
            "Unary    : operator: Token, right: Expression",
            "Variable : name: Token",
        ),
    )
    generator.defineAst(
        outputDir,
        "Statement",
        listOf(
            "Block               : statements: List<Statement>",
            "Class               : name: Token, superclass: Expression.Variable?, methods: List<Function>",
            "ExpressionStatement : expression: Expression",
            "Function            : name: Token, parameters: List<Token>, body: List<Statement>",
            "If                  : condition: Expression, thenBranch: Statement, elseBranch: Statement?",
            "Print               : expression: Expression",
            "Return              : keyword: Token, value: Expression?",
            "Variable            : name: Token, initializer: Expression?",
            "While               : condition: Expression, body: Statement"
        ),
    )
}

class Generator {
    fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir/$baseName.kt"
        PrintWriter(path, "UTF-8").use { writer ->
            writer.println("package dev.edd255.lox")
            writer.println()
            writer.println("abstract class $baseName {")
            writer.println("    abstract fun <T> accept(visitor: Visitor<T>): T")
            for (type in types) {
                val className = type.split(":")[0].trim()
                val fields = type.split(":", ignoreCase = true, limit = 2)[1].trim()
                defineType(writer, baseName, className, fields)
            }
            defineVisitor(writer, baseName, types)
            writer.print("}")
        }
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
        writer.println()
        writer.print("    class $className(")
        val fields = fieldList.split(", ").iterator()
        while (fields.hasNext()) {
            val field = fields.next()
            if (fields.hasNext()) {
                writer.print("val $field, ")
            } else {
                writer.print("val $field")
            }
        }
        writer.println(") : $baseName() {")
        writer.println("        override fun <T> accept(visitor: Visitor<T>): T = visitor.${visitorMethodName(baseName, className)}(this)")
        writer.println("    }")
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println()
        writer.println("    interface Visitor<T> {")
        for (type in types) {
            val typeName = type.split(":")[0].trim()
            val parameterType = if (baseName == "Statement" && typeName == "Class") "Statement.Class" else typeName
            writer.println("        fun ${visitorMethodName(baseName, typeName)}(${parameterName(baseName, typeName)}: $parameterType): T")
        }
        writer.println("    }")
    }

    private fun visitorMethodName(baseName: String, typeName: String): String {
        val suffix = if (typeName.endsWith(baseName)) "" else baseName
        return "visit$typeName$suffix"
    }

    private fun parameterName(baseName: String, typeName: String): String {
        return when ("$baseName.$typeName") {
            "Expression.Super" -> "superExpression"
            "Expression.This" -> "thisStatement"
            "Statement.Class" -> "classStatement"
            "Statement.If" -> "ifQuery"
            "Statement.Return" -> "returnStatement"
            "Statement.While" -> "whileLoop"
            else -> typeName.replaceFirstChar { it.lowercase() }
        }
    }
}
