# C- Compiler Checkpoint 2

---

## Requirements

This project is intended to run on linux.socs.uoguelph.ca.
Compilation and testing were performed on that server.

`java`, `jflex`, and `make` must be available in the system path.

What is needed:
- Java 8 or later
- JFlex
- Java CUP (java-cup-0.11b.jar)

## Build

Compile the project:

```
make
```

This will:

* generate `parser.java` and `sym.java` from `grammar/parser.cup`
* generate `Lexer.java` from `grammar/cminus.flex`
* compile all Java files into the `build/` directory

## Clean

Remove generated and compiled files:

```
make clean
```

---

## Run the Compiler

Run the compiler with AST output:

```
java -cp build:java-cup-0.11b.jar CM -a tests/semantic_tests/1.cm 
```

Run the compiler with symbol table output:
```
java -cp build:java-cup-0.11b.jar CM -s tests/semantic_tests/1.cm 
```

Run the compiler with AST and symbol table output:
```
java -cp build:java-cup-0.11b.jar CM -a -s tests/semantic_tests/1.cm 
```

Notes:

- Replace `1.cm` with any other test program in `tests/semantic_tests/`.
- The -a flag generates an abstract syntax tree file (.abs) for the input program, which gets output to the `tests/semantic_tests` directory.
- The -s flag generates a symbol table file (.sym) for the input program, which gets output to same `tests/semantic_tests`  directory.

---

## Run the Scanner

To print tokens from the scanner:

```
java -cp build:java-cup-0.11b.jar Scanner < tests/scanner_tests/test_basic.cm
```

---

## Test Programs

The repository includes five semantic test programs found in `tests/semantic_tests`:

1.cm - valid program with no semantic errors
2.cm - program demonstrating 3 semantic errors
3.cm - program demonstrating 3 semantic errors
4.cm - program demonstrating 3 semantic errors
5.cm - program with multiple semantic errors