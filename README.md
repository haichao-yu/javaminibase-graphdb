# JavaMinibase-GraphDBMS

This is a course project of CSE 510 (Database Management System Implementation) of Arizona State University for 2017 Spring.
The goal of this project is to use the modules of Java Minibase as building blocks for implementing a graph DBMS.
This project including 3 phases:

* **Phase 1:** Figure out the internal implementation of Java Minibase, including file organization (heap files, sorted files, indexes), buffer management, disk management, etc. 
* **Phase 2:** Implement data forms of graph DBMS (node and edge) and related methods, such as comparing, external sorting, insertion, deletion, data printing, etc.
* **Phase 3:** Implement index-nested-loop join and sort-merge join specific for nodes and edges. Utilizing join operations to implement complex query operations (Path Expression Query, Triangle Query).

## Data Storage Format

Java Minibase stores data in the form of **tuples**. The graph DBMS, on the other hand, stores data in the form of **nodes** and **edges**.

#### node
Node has the form of **(node_label, node_descriptor)**, where node_label is a string, node_descriptor is a 5-dimensional vector.

#### edge
Edge has the form of **(srcNID, dstNID, edge_label, edge_weight)**, where srcNID is the NID of source node, dstNID is the NID of destination node, edge_label is a string, edge_weight is an integer.

## Usage

1. Modify the "Makefile"s to reflect your JDK path;
2. Open your terminal and enter the "src" directory;
```
cd src/
```
3. Use following command to enter to program;
```
make graphtest
```
4. Start to test and have fun!
```
/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home//bin/javac -classpath .:.. GraphTest.java GraphDBManager.java Util.java BatchEdgeDeletion.java BatchEdgeInsertion.java BatchNodeDeletion.java BatchNodeInsertion.java SimpleEdgeQuery.java    SimpleNodeQuery.java
/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home//bin/java  -classpath .:.. graphtests.GraphTest
Please input your command (input "help" to learn supported operations, input "exit" to exit):
```
5. After test, you can use following command to clean the .class files
```
make clean
```

## Operations

This graph DBMS support following opearations:

#### Batch Node Insert
```
batchnodeinsert NODE_FILE_NAME GRAPHDB_NAME
```
The format of the node insertion data file will be as follows:
```
nodelabel1 nodedesc11 nodedesc12 nodedesc13 nodedesc14 nodedesc15
nodelabel2 nodedesc21 nodedesc22 nodedesc23 nodedesc24 nodedesc25
...
```

#### Batch Edge Insert
```
batchedgeinsert EDGE_FILE_NAME GRAPHDB_NAME
```
The format of the edge insertion data file will be as follows:
```
sourcelabel1 destlabel1 edgelabel1 edgeweight1
sourcelabel2 destlabel2 edgelabel2 edgeweight2
...
```

#### Batch Node Delete
```
batchnodedelete NODE_FILE_NAME GRAPHDB_NAME
```
The format of the node deletion data file will be as follows:
```
nodelabel1
nodelabel2
...
```

#### Batch Edge Deletion
```
batchedgedelete EDGE_FILE_NAME GRAPHDB_NAME
```
The format of the edge deletion data file will be as follows:
```
sourcelabel1 destlabel1 edgelabel1
sourcelabel2 destlabel2 edgelabel2
...
```

#### Simple Node Query
```
nodequery GRAPHDB_NAME NUMBUF QTYPE INDEX [QUERYOPTIONS]
```
* If QTYPE = 0, then the query will print the node data in the order it occurs in the node heap file.
* If QTYPE = 1, then the query will print the node data in increasing alphanumerical order of labels.
* If QTYPE = 2, then the query will print the node data in increasing order of distance from a given 5D target descriptor.
* If QTYPE = 3, then the query will take a target descriptor and a distance and return the labels of nodes within the given distance from the target descriptor.
* If QTYPE = 4, then the query will take a label and return all relevant information (including outgoing and incoming edges) about the node with the matching label (if any).
* If QTYPE = 5, then the query will take a target descriptor and a distance and return all relevant information (including outgoing and incoming edges) about the nodes within the given distance from the target descriptor.

If INDEX = 1, the query will be processed using an index, otherwise only the relevant heap files will be used.

Minibase will use __at most__ NUMBUF buffer pages to run the query.

At the end of the query, the program will output the number of disk pages that were read and written (separately).

#### Simple Edge Query
```
edgequery GRAPHDB_NAME NUMBUF QTYPE INDEX [QUERYOPTIONS]
```
* If QTYPE = 0, then the query will print the edge data in the order it occurs in the edge heap file.
* If QTYPE = 1, then the query will print the edge data in increasing alphanumerical order of source node labels.
* If QTYPE = 2, then the query will print the edge data in increasing alphanumerical order of destination node labels.
* If QTYPE = 3, then the query will print the edge data in increasing alphanumerical order of edge labels.
* If QTYPE = 4, then the query will print the edge data in increasing order of edge weights.
* If QTYPE = 5, then the query will take a lower and upper bound on edge weights, and will print the matching edge data.
* If QTYPE = 6, then the query will print pairs of incident graph edges.

If INDEX = 1, the query will be processed using an index, otherwise only the relevant heap files will be used.

Minibase will use __at most__ NUMBUF buffer pages to run the query.

At the end of the query, the program will output the number of disk pages that were read and written (separately).

#### Path Expression Query, Type 1
```
PQ1a GRAPHDB_NAME NN/.../NN
```
*NN* <- (*node_label*|*node_descriptor*)

This query will returns the labels of the nodes in the head and tail of each path that satisfies the path expression.
```
PQ1b GRAPHDB_NAME NN/.../NN
```
This query will returns the same data, but sorts the results in the order of source and tail labels.
```
PQ1c GRAPHDB_NAME NN/.../NN
```
This query will returns the same data, but only **distinct** head/tail node label pairs.

#### Path Expression Query, Type 2
```
PQ2a GRAPHDB_NAME NN/EN/.../EN
```
*NN* <- (*node_label*|*node_descriptor*)

*EN* <- (*edge_label*|*max_edge_weight*)

This query will returns the labels of the nodes in the head and tail of each path that satisfies the path expression.
```
PQ2b GRAPHDB_NAME NN/EN/.../EN
```
This query will returns the same data, but sorts the results in the order of source and tail labels.
```
PQ2c GRAPHDB_NAME NN/EN/.../EN
```
This query will returns the same data, but only **distinct** head/tail node label pairs.

#### Path Expression Query, Type 3
```
PQ3a GRAPHDB_NAME NN//Bound
```
*NN* <- (*node_label*|*node_descriptor*)

*Bound* <- (*max_num_edges*|*max_total_edge_weight*)

This query will returns the labels of the nodes in the head and tail of each path that satisfies the path expression.
```
PQ3b GRAPHDB_NAME NN//Bound
```
This query will returns the same data, but sorts the results in the order of source and tail labels.
```
PQ3c GRAPHDB_NAME NN//Bound
```
This query will returns the same data, but only **distinct** head/tail node label pairs.

#### Triangle Query
```
TQa GRAPHDB_NAME EN;EN;EN
```
*EN* <- (*edge_label*|*max_edge_weight*)

For each corresponding triangle, this query will returns the labels of the corresponding 3 nodes. 
```
TQb GRAPHDB_NAME EN;EN;EN
```
This query will returns the same data, but sorts the results in the labels of the corresponding nodes.
```
TQc GRAPHDB_NAME EN;EN;EN
```
This query will returns the same data, but only distinct node label triples.

## More Details

If you have any problem in test, please refer to the test sample file.

If you want to learn more implementation details, please refer to the docs.