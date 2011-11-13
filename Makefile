LIB_ANTLR := lib/antlr.jar
ANTLR_SCRIPT := src/MicroParser.g

all: 	group compiler 
	rm -rf classes
	mkdir classes
	javac -cp $(LIB_ANTLR) -d classes src/*.java generated/src/*.java -Xlint:unchecked

group:
	@more README	

compiler:
	java -cp $(LIB_ANTLR) org.antlr.Tool $(ANTLR_SCRIPT) -o generated 

clean:
	rm -rf classes MicroParser.tokens src/MicroParser.java generated

.PHONY: all group compiler clean
