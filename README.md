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


To output to a specific file, add `> output.txt` to the end of the above command like so:

```
java -cp build:java-cup-0.11b.jar CM -a tests/semantic_tests/1.cm > output.txt
```

Notes:
- The -a flag runs the parser and semantic analyzer and saves the abstract syntax tree into an output file.
- Replace `1.cm` with any other test program in `tests/semantic_tests/`.
- The -s flag does both parsing and  semantic analysis and saves the symbol table  into an output file.

---

## Run the Scanner

To print tokens from the scanner:

```
java -cp build:java-cup-0.11b.jar Scanner < tests/scanner_tests/test_basic.cm
```
