package org.edd255.tool

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
            "Binary   : left: Expr, op: Token, right: Expr",
            "Grouping : expr: Expr",
            "Literal  : value: Any",
            "Unary    : op: Token, right: Expr"
        )
    )
}

class Generator {
    fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")

        writer.println("package org.edd255.lox.expr")
        writer.println()
        writer.println("import org.edd255.lox.Token")
        writer.println()
        writer.println("abstract class $baseName {")
        writer.println("    abstract fun <T> accept(visitor: Visitor<T>): T")
        writer.println("}")

        for (type in types) {
            val className = type.split(":")[0].trim()
            val fields = type.split(":", ignoreCase = true, limit = 2)[1].trim()
            defineType(writer, baseName, className, fields)
            writer.println()
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
                writer.print("val $field, ")
            } else {
                writer.print("val $field")
            }
        }
        writer.println(") : $baseName() {")
        writer.println("    override fun <T> accept(visitor: Visitor<T>): T {")
        writer.println("        return visitor.visit$className$baseName(this)")
        writer.println("    }")

        writer.println("}")
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println()
        writer.println("interface Visitor<T> {")
        for (type in types) {
            val typeName = type.split(":")[0].trim()
            writer.println("    fun visit$typeName$baseName(${baseName.lowercase(Locale.getDefault())}: $typeName): T")
        }
        writer.println("}")
    }
}
