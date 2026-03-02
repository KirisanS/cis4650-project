# c- compiler cp1

## files

### cminus.flex  
jflex specification for the scanner. defines tokens using regular expressions and returns cup symbols.

### parser.cup  
cup grammar specification. defines terminals, non-terminals, and grammar rules. generates parser.java and sym.java.

### main.java
driver for the parser. creates a lexer and parser, parses the input file, and runs the compiler pipeline.

### scanner.java  
standalone scanner test driver. reads a file and prints tokens for debugging.

## how to build

make clean  
make

## how to "run" parser

java -cp .:java-cup-0.11b.jar main CM parser_tests/test_basic.cm

## how to run scanner only

java -cp .:java-cup-0.11b.jar scanner < scanner_tests/test_basic.cm

## how to run .cm programs
 java -cp .:java-cup-0.11b.jar CM -a  parser_tests/inputnumber.cm > output.txt
 - inputnumber must be replaced with coresponding number or choen file
 - output will be placed in output.txt