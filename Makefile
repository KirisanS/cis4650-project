JAVAC = javac
JFLEX = jflex
CUPJAR = java-cup-0.11b.jar
CP = .:$(CUPJAR)

all: parser.java Lexer.java
	$(JAVAC) -cp $(CP) absyn/*.java ShowTreeVisitor.java Lexer.java parser.java sym.java CM.java Scanner.java

parser.java sym.java: parser.cup
	java -jar $(CUPJAR) -parser parser -symbols sym parser.cup

Lexer.java: cminus.flex
	$(JFLEX) cminus.flex

clean:
	rm -f Lexer.java parser.java sym.java *.class absyn/*.class *~