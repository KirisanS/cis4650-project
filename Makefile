JAVAC = javac
JFLEX = jflex
CUPJAR = java-cup-0.11b.jar
CP = .:$(CUPJAR)

all: parser.java Lexer.java
	$(JAVAC) -cp $(CP) Lexer.java parser.java sym.java Main.java Scanner.java

parser.java sym.java: parser.cup
	java -jar $(CUPJAR) parser.cup

Lexer.java: cminus.flex
	$(JFLEX) cminus.flex

clean:
	rm -f Lexer.java parser.java sym.java *.class *~