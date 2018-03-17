# Variables
myBot=MyBot
file=$(myBot).java
src=./src
bin=./bin
logs=./logs
replays=./replays
debugInfo=-g:source,lines,vars
remoteAccess=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1050,quiet=y

# Rules
all: build

build: $(file)

$(file): $(src)/$(file)
	cp -r $(src)/* $(bin)/
	javac -cp $(bin)/ $(bin)/$(file)
	#find $(bin)/ -type f -name '*.java' -delete

run:
	javac -cp $(bin)/ $(bin)/TestBot.java
	./halite -d "1200 700" "java -cp $(bin) $(myBot)" "java -cp $(bin) TestBot"
	mv *.log $(logs)/
	mv *.hlt $(replays)/

debug:
	javac $(debugInfo) -d $(bin) -cp $(src)/ $(src)/$(file)
	./halite -s 234785527 -t -d "384 256" "java -cp $(bin) $(remoteAccess) $(myBot)"
	mv *.log $(logs)/
	mv *.hlt $(replays)/

clean: 
	rm -fr $(bin)/* $(logs)/*

.PHONY: all build clean debug run

