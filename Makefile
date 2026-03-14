JAVAC = javac
JFLEX = jflex
CUPJAR = java-cup-0.11b.jar
CP = .:$(CUPJAR)

all: generated/parser.java generated/Lexer.java
	mkdir -p build
	$(JAVAC) -cp $(CP) -d build \
	src/absyn/*.java \
	src/*.java \
	generated/*.java

generated/parser.java generated/sym.java: grammar/parser.cup
	mkdir -p generated
	java -jar $(CUPJAR) -parser parser -symbols sym grammar/parser.cup
	mv parser.java sym.java generated/

generated/Lexer.java: grammar/cminus.flex
	mkdir -p generated
	$(JFLEX) grammar/cminus.flex
	mv grammar/Lexer.java generated/

clean:
	rm -rf build generated *~