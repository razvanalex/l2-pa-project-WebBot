.PHONY: build clean run

 build: MyBot

 run:
	@java -Xmx1G MyBot $(ARGS)

 MyBot:
	javac MyBot.java

 clean:
	rm -rf *.class ./hlt/*.class *.log
