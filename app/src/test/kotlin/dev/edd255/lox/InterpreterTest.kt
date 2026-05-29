package dev.edd255.lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InterpreterTest {
    @Test
    fun `evaluates arithmetic strings and truthiness`() {
        assertRuns(
            """
            print 1 + 2 * 3;
            print (1 + 2) * 3;
            print "lo" + "x";
            print !nil;
            print !false;
            print !!true;
            """.trimIndent(),
            """
            7
            9
            lox
            true
            true
            true
            
            """.trimIndent(),
        )
    }

    @Test
    fun `evaluates variables and lexical scope`() {
        assertRuns(
            """
            var value = "global";
            {
                var value = "block";
                print value;
            }
            print value;
            """.trimIndent(),
            """
            block
            global
            
            """.trimIndent(),
        )
    }

    @Test
    fun `evaluates control flow`() {
        assertRuns(
            """
            var total = 0;
            for (var i = 0; i < 4; i = i + 1) {
                total = total + i;
            }
            print total;
            if (total == 6 and true) print "ok"; else print "bad";
            while (total > 0) {
                total = total - 2;
            }
            print total;
            """.trimIndent(),
            """
            6
            ok
            0
            
            """.trimIndent(),
        )
    }

    @Test
    fun `evaluates functions recursion and closures`() {
        assertRuns(
            """
            fn fib(n) {
                if (n <= 1) return n;
                return fib(n - 2) + fib(n - 1);
            }
            print fib(6);

            fn makeCounter() {
                var i = 0;
                fn count() {
                    i = i + 1;
                    return i;
                }
                return count;
            }
            var counter = makeCounter();
            print counter();
            print counter();
            """.trimIndent(),
            """
            8
            1
            2
            
            """.trimIndent(),
        )
    }

    @Test
    fun `evaluates classes initializers methods and fields`() {
        assertRuns(
            """
            class Bag {
                fn init(value) {
                    this.value = value;
                }

                fn get() {
                    return this.value;
                }
            }

            var bag = Bag("lox");
            bag.extra = "field";
            print bag.get();
            print bag.extra;
            """.trimIndent(),
            """
            lox
            field
            
            """.trimIndent(),
        )
    }

    @Test
    fun `evaluates inheritance super and this`() {
        assertRuns(
            """
            class A {
                fn method() {
                    return "A";
                }
            }

            class B: A {
                fn method() {
                    return "B";
                }

                fn test() {
                    return super.method() + this.method();
                }
            }

            print B().test();
            """.trimIndent(),
            """
            AB
            
            """.trimIndent(),
        )
    }

    @Test
    fun `evaluates native functions`() {
        val result = runLox(
            """
            print chr(65);
            print print_error("diagnostic");
            """.trimIndent()
        )

        assertFalse(result.hadError, result.stderr)
        assertFalse(result.hadRuntimeError, result.stderr)
        assertEquals("A\nnil\n", result.stdout)
        assertEquals("diagnostic\n", result.stderr)
    }

    @Test
    fun `reports invalid native function argument types as runtime errors`() {
        for ((source, expectedMessage) in listOf(
            """chr("x");""" to "Argument 1 to 'chr' must be a number.",
            "exit(nil);" to "Argument 1 to 'exit' must be a number.",
        )) {
            val result = runLox(source)

            assertFalse(result.hadError, result.stderr)
            assertTrue(result.hadRuntimeError)
            assertEquals("", result.stdout)
            assertTrue(result.stderr.contains(expectedMessage), result.stderr)
            assertTrue(result.stderr.contains("[line 1]"), result.stderr)
        }
    }

    @Test
    fun `reports runtime errors without executing following statements`() {
        val result = runLox(
            """
            print missing;
            print "after";
            """.trimIndent()
        )

        assertFalse(result.hadError, result.stderr)
        assertTrue(result.hadRuntimeError)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.contains("Undefined variable 'missing'"), result.stderr)
    }

    private fun assertRuns(source: String, expectedStdout: String) {
        val result = runLox(source)
        assertFalse(result.hadError, result.stderr)
        assertFalse(result.hadRuntimeError, result.stderr)
        assertEquals(expectedStdout, result.stdout)
        assertEquals("", result.stderr)
    }
}
