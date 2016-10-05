TestDriver: TestDriver.class
	java -classpath '.:/home/raphi/crawler_tester/crawler4j/target/*' TestDriver

TestDriver.class: TestDriver.java TestCrawler.class
	javac -classpath '.:/home/raphi/crawler_tester/crawler4j/target/*' TestDriver.java

TestCrawler.class: TestCrawler.java
	javac -classpath '.:/home/raphi/crawler_tester/crawler4j/target/*' TestCrawler.java

.PSEUDO: clean test 

clean: 
	rm *.class

test: 

