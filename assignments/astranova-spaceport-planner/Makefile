JAVAC=javac
JAVA=java
SRC_DIR=src
BUILD_DIR=build
MAIN=Main

.PHONY: all run clean

all:
	mkdir -p $(BUILD_DIR)
	$(JAVAC) -d $(BUILD_DIR) $(SRC_DIR)/*.java

run: all
	$(JAVA) -cp $(BUILD_DIR) $(MAIN) examples/launch_plans_sample.xml examples/spaceport_transit_sample.dat

clean:
	rm -rf $(BUILD_DIR)
