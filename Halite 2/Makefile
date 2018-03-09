# Variables
myBot=MyBot
file=$(myBot).java
src=./src
bin=./bin
logs=./logs
replays=./replays

# Rules
all: build

build: $(file)

$(file): $(src)/$(file)
	cp -r $(src)/* $(bin)/
	javac -cp $(bin)/ $(bin)/$(file)
	find $(bin)/ -type f -name '*.java' -delete

run:
	./halite -d "240 160" "java -cp $(bin) $(myBot)" "java -cp $(bin) $(myBot)"
	mv *.log $(logs)/
	mv *.hlt $(replays)/

clean: 
	rm -fr $(bin)/* $(logs)/*

.PHONY: all build clean run

