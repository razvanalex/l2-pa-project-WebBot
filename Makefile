.PHONY: build clean run

 build: MyBot

 run:
	time @java -Xmx1G MyBot $(ARGS)

 MyBot:
	javac MyBot.java

 clean:
	rm -rf *.class ./hlt/*.class *.log
