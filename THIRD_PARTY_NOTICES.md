# Third-Party Notices

## Crafting Interpreters

Lox is the programming language developed by Robert Nystrom for [*Crafting Interpreters*](https://craftinginterpreters.com/).

This repository implements the Lox interpreter from the book in Kotlin. 
The language design, terminology and baseline interpreter pipeline are attributed to *Crafting Interpreters*. 
Repository-specific work includes the Kotlin implementation structure, CLI behavior, additional native functions, fixture harness, regression tests and GraalVM Native Image packaging.

## LoxLox

`app/src/test/lox/lox.lox` and most `.lox` files under `app/src/test/lox/` are marked as adapted from Ben Hoyt's `loxlox` project or test suite.
Those files carry per-file MIT SPDX and copyright comments.
The corresponding MIT license text is preserved in `app/src/test/lox/LOXLOX_LICENSE.txt`.
