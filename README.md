# C- Compiler Checkpoint 3

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
java -cp build:java-cup-0.11b.jar CM -a tests/code_tests/1.cm 
```

Run the compiler with symbol table output:
```
java -cp build:java-cup-0.11b.jar CM -s tests/code_tests/1.cm 
```

Run the compiler with AST and symbol table output:
```
java -cp build:java-cup-0.11b.jar CM -a -s tests/code_tests/1.cm 
```

Run the compiler with TM instructions output:
```
java -cp build:java-cup-0.11b.jar CM -c tests/code_tests/1.cm 
```

### Running TM Code

After generating TM code with `-c`, you can execute it using:
```
./TMSimulator/tm tests/code_tests/1.tm
```

Once in the simulator, type "h" for the list of available commands.  In particular, type "g" to run the program, and the output should be displayed on the screen. To run the program again, type "c" to clear up the environment and type "g" to run the program.

Notes:

- Replace `1.cm` with any other test program in `tests/code_tests/`
- The -a flag generates an abstract syntax tree file (.abs) for the input program, which gets output to the `tests/code_tests` directory.
- The -s flag generates a symbol table file (.sym) for the input program, which gets output to the same `tests/code_tests`  directory.
- The -c flag generates TM assembly code (.tm) for the input program. The output file is saved alongside the input program (e.g., `1.cm` to `1.tm`), and can be executed using the TM simulator.

---

## Run the Scanner

To print tokens from the scanner:

```
java -cp build:java-cup-0.11b.jar Scanner < tests/scanner_tests/test_basic.cm
```

---

## Test Programs

The repository includes ten C- test programs located in `tests/code_tests/`:

1. 1.cm – valid program (generates correct TM code and executes successfully)
2. 2.cm – valid program (moderate complexity)
3. 3.cm – valid program (more complex features such as control flow or function calls)

4. 4.cm – program with syntactic errors
5. 5.cm – program with semantic errors
6. 6.cm – program with semantic errors
7. 7.cm – program with runtime errors (e.g., invalid operations)
8. 8.cm – program with runtime errors (e.g., array bounds)

9. 9.cm – stress test (complex or mixed errors)
10. 0.cm – stress test (no restrictions on errors)

Each test file includes a comment header describing:
- what feature is being tested
- the expected behavior or error(s)