# modify the paths appropriately
JDKPATH = /Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home/
LIBPATH = .:..
CLASSPATH = $(LIBPATH)
BINPATH = $(JDKPATH)/bin
JAVAC = $(JDKPATH)/bin/javac -classpath $(CLASSPATH)
JAVA  = $(JDKPATH)/bin/java  -classpath $(CLASSPATH)

# test buffer manager

GraphTest:GraphTest.java
	$(JAVAC) GraphTest.java GraphDBManager.java Util.java BatchEdgeDeletion.java BatchEdgeInsertion.java BatchNodeDeletion.java BatchNodeInsertion.java SimpleEdgeQuery.java    SimpleNodeQuery.java

graphtest: GraphTest
	$(JAVA) graphtests.GraphTest
