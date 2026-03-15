# C- Compiler

## Requirements

This project has been tested on the linux server: linux.socs.uoguelph.ca

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

---

## Clean

Remove generated and compiled files:

```
make clean
```

---

## Run the Compiler

Run the compiler with AST output and semantic analysis:

```
java -cp build:java-cup-0.11b.jar CM -a tests/semantic_tests/1.cm
```

To output to a file, add `> output.txt` to the end of the above command like so:

```
java -cp build:java-cup-0.11b.jar CM -a tests/semantic_tests/1.cm > output.txt
```

Notes:
- The -a flag runs the parser and semantic analyzer and prints the abstract syntax tree.
- Replace `1.cm` with any other test program in `tests/semantic_tests/`.

---

## Run the Scanner

To print tokens from the scanner:

```
java -cp build:java-cup-0.11b.jar Scanner < tests/scanner_tests/test_basic.cm
```
