package dev.edd255.tools

import java.io.PrintWriter
import java.util.Locale
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
        "Expr",
        listOf(
            "Assign   : name: Token, value: Expr",
            "Binary   : left: Expr, op: Token, right: Expr",
            "Grouping : expr: Expr",
            "Literal  : value: Any?",
            "Unary    : op: Token, right: Expr",
            "Variable : name: Token",
        ),
    )
    generator.defineAst(
        outputDir,
        "Stmt",
        listOf(
            "Block    : stmts: List<Stmt>",
            "ExprStmt : expr: Expr",
            "Print    : expr: Expr",
            "Var      : name: Token, initializer: Expr?",
        ),
    )
}

class Generator {
    fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")
        writer.println("package dev.edd255.lox.expr")
        writer.println()
        writer.println("import dev.edd255.lox.Token")
        writer.println()
        writer.println("abstract class $baseName {")
        writer.println("    abstract fun <T> accept(visitor: ${baseName}Visitor<T>): T")
        writer.println("}")
        for (type in types) {
            val className = type.split(":")[0].trim()
            val fields = type.split(":", ignoreCase = true, limit = 2)[1].trim()
            defineType(writer, baseName, className, fields)
        }
        defineVisitor(writer, baseName, types)
        writer.close()
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
        writer.println()
        writer.print("class $className(")
        val fields = fieldList.split(", ").iterator()
        while (fields.hasNext()) {
            val field = fields.next()
            if (fields.hasNext()) {
                writer.print("private val $field, ")
            } else {
                writer.print("private val $field")
            }
        }
        writer.println(") : $baseName() {")
        if (className == "ExprStmt") {
            writer.println("    override fun <T> accept(visitor: ${baseName}Visitor<T>): T = visitor.visit$className(this)")
        } else {
            writer.println("    override fun <T> accept(visitor: ${baseName}Visitor<T>): T = visitor.visit$className$baseName(this)")
        }
        val parts = fieldList.split(",").map { it.trim() }
        for (part in parts) {
            val keyValue = part.split(":").map { it.trim() }
            val name = keyValue[0]
            val type = keyValue[1]
            writer.println("    fun get${capitalizeFirstChar(name)}(): $type = $name")
        }
        writer.println("}")
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println()
        writer.println("interface ${baseName}Visitor<T> {")
        for (type in types) {
            val typeName = type.split(":")[0].trim()
            if (typeName == "ExprStmt") {
                writer.println("    fun visit$typeName(${baseName.lowercase(Locale.getDefault())}: $typeName): T")
            } else {
                writer.println("    fun visit$typeName$baseName(${baseName.lowercase(Locale.getDefault())}: $typeName): T")
            }
        }
        writer.println("}")
    }

    private fun capitalizeFirstChar(input: String): String {
        if (input.isEmpty()) {
            return input
        }
        val firstChar = input[0].uppercaseChar()
        val restOfChars = input.substring(1)
        return "$firstChar$restOfChars"
    }
}
