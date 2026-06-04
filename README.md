# Lox Interpreter, implemented in Kotlin

A tree-walk interpreter for the Lox programming language, implemented in Kotlin and based on the interpreter developed in Robert Nystrom's [*Crafting Interpreters*](https://craftinginterpreters.com/).

This implementation
* ... adds a Kotlin-specific structure
* ... adds native executable compilation through GraalVM
* ... adds additional native functions
* ... adds regression coverage
* ... uses [Ben Hoyt's the test harness](https://github.com/benhoyt/loxlox/tree/master/test)
* ... and runs [Ben Hoyt's Lox interpreter implemented in Lox](https://github.com/benhoyt/loxlox) over the tests.

## Run It

Run an example program:

```bash
./gradlew :app:run --args="examples/fibonacci.lox"
```

Start the interactive prompt:

```bash
./gradlew :app:run
```

Run the suite with:

```bash
./gradlew test
```

Build a native executable when running on a GraalVM JDK with Native Image support:

```bash
./gradlew :app:nativeCompile
./app/build/native/nativeCompile/lox examples/fibonacci.lox
```

If Gradle does not detect the intended GraalVM installation, point toolchain detection at it:

```bash
./gradlew -Dorg.gradle.java.installations.paths="$GRAALVM_HOME" :app:nativeCompile
```

## Command Line

```
Usage: lox [options] [script]

Options:
  -h, --help       Show help.
  --version        Show the interpreter version.
  --quiet          Start the REPL without the banner.
  --no-banner      Alias for --quiet.
```

## Native Functions

In addition to `clock`, this interpreter provides:

* `getc()`: read one UTF-8 character code from standard input, or `-1` at end of input or on read failure.
* `chr(number)`: convert a numeric character code to a one-character string.
* `exit(number)`: terminate the REPL with the given exit status.
* `print_error(value)`: print a value to standard error and return `nil`.
* `__instance_class_name(value)`: return an instance's class name, or an empty string for non-instances.
* `__value_type(value)`: return a runtime type label.
