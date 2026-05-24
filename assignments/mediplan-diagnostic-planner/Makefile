JAVAC=javac
JAVA=java
SRC_DIR=src
BIN_DIR=bin
CATALOGUE=examples/diagnostic_catalogue_simple.xml
REQUESTS=examples/diagnostic_requests_simple.xml

.PHONY: compile run clean

compile:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SRC_DIR)/*.java

run: compile
	$(JAVA) -cp $(BIN_DIR) Main $(CATALOGUE) $(REQUESTS)

clean:
	rm -rf $(BIN_DIR)
