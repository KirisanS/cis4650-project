# C- Compiler

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

Run the compiler with AST output:

```
java -cp build:java-cup-0.11b.jar CM -a tests/parser_tests/1.cm
```

Replace `1.cm` with any other test program in `tests/parser_tests/`.

---

## Run the Scanner

To print tokens from the scanner:

```
java -cp build:java-cup-0.11b.jar Scanner < tests/scanner_tests/test_basic.cm
```

---

## Run the Symbol Table Tester

To print tokens from the scanner:

```
java -cp build:java-cup-0.11b.jar SymbolTableManualTest
```

