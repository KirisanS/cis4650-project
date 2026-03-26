JAVAC = javac
JFLEX = jflex
CUPJAR = java-cup-0.11b.jar
CP = .:$(CUPJAR)

all: generated/parser.java generated/Lexer.java
	mkdir -p build

# NOTE
# If you add new Java files to the project, place them inside:
#   src/
#   src/absyn/
# These patterns automatically include them:
#   src/*.java
#   src/absyn/*.java
# If you add files in a NEWLY made folder (for example src/semantics/),
# you must add it to the list right below $(JAVAC) -cp $(CP) -d build \,
#  and that folder would look like:
#   src/semantics/*.java \

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
	find . -name "*.abs" -delete
	find . -name "*.sym" -delete
	find . -name "*.tm" -delete

a: clean all