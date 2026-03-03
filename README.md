# c- compiler checkpoint 1

## overview

this project implements the front-end of a c- compiler, including lexical analysis using jflex, syntax analysis using cup, and abstract syntax tree (ast) construction and printing.

the scanner produces tokens compatible with cup, the parser builds the ast, and a visitor is used to print the tree structure.

---

## testing environment

this project has been tested on the linux server: linux.socs.uoguelph.ca

`java`, `jflex`, and `make` must be available in the system path.

## build instructions

### to build the project:

```bash
make
```

this will:
1. generate lexer.java from cminus.flex using jflex
2. generate parser.java and sym.java from parser.cup using cup
3. compile all java source files

### to clean generated files:

```bash
make clean
```

this removes:
- lexer.java
- parser.java
- sym.java
- all .class files

---

## testing the components

### scanner

to print tokens produced by the scanner:

```bash
java -cp .:java-cup-0.11b.jar Scanner < scanner_tests/test_basic.cm  
```

this reads input from standard input and prints token names.


### parser

```bash
java -cp .:java-cup-0.11b.jar Main parser_tests/test_basic.cm
```
this parses the file and prints the abstract syntax tree.



## how to run: the full compiler driver

```bash
java -cp .:java-cup-0.11b.jar CM -a parser_tests/1.cm
```

the -a flag enables abstract syntax tree printing.
- `1.cm` may be replaced with the other test files found in the same directory, ie., `2.cm, 3.cm, 4.cm, 5.cm`
- output will be placed in output.txt

### how to: output the parse tree to a file

```bash
java -cp .:java-cup-0.11b.jar CM -a parser_tests/1.cm > output.txt
```

the -a flag enables abstract syntax tree printing.
- `1.cm` may be replaced with the other test files found in the same directory, ie., `2.cm, 3.cm, 4.cm, 5.cm`
- output will be placed in output.txt
- syntax errors are printed to stderr

---

## test programs

five required test programs are included:

1. `1.cm`: valid program (no errors)  
2. `2.cm`: lexical and/or syntax errors (max 3)  
3. `3.cm`: different error cases  
4. `4.cm`: additional distinct error cases  
5. `5.cm`: arbitrary errors (no limit) 

each file includes a comment header explaining the errors it is designed to test.

## file description

### cminus.flex  
jflex specification for the scanner. defines tokens using regular expressions and returns cup symbols.

### parser.cup  
cup grammar specification. defines terminals, non-terminals, and grammar rules. generates parser.java and sym.java.

### cm.java  
main compiler driver with optional ast printing.

### main.java
driver for the parser. creates a lexer and parser, parses the input file, and runs the compiler pipeline.

### scanner.java  
standalone scanner test driver. reads a file and prints tokens for debugging.

### showtreevisitor.java  
visitor implementation used to print the ast.

---

## acknowledgement
a sample parser for the tiny programming language was provided in c1-package.tgz on courselink.
that sample implementation was used as a structural reference for integrating jflex and cup, setting up the parser driver, and implementing the visitor pattern.
all grammar rules, token definitions, and ast structures for the c- language were developed and adapted specifically for this project.