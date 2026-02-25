JAVAC ?= javac
JFLEX ?= jflex
CUPJAR = java-cup-0.11b.jar
CP = .:$(CUPJAR)

all: Lexer.java
	$(JAVAC) -cp $(CP) *.java

Lexer.java: cminus.flex
	$(JFLEX) cminus.flex

run: all
	java -cp $(CP) Scanner < test.cm

clean:
	rm -f Lexer.java *.class *~