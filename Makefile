JAVAC ?= javac
JFLEX ?= jflex

all: Token.class Lexer.class Scanner.class

%.class: %.java
	$(JAVAC) $<

Lexer.java: warmup.flex
	$(JFLEX) $<

clean:
	rm -f Lexer.java *.class *~