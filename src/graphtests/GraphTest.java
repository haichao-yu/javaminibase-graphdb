package graphtests;

import global.SystemDefs;

import edgeheap.Edge;
import global.AttrType;
import heap.Tuple;
import iterator.*;


import java.io.IOException;

/**
 * Created by yhc on 3/14/17.
 */
public class GraphTest {


    public static void main(String[] args) {

        // detete all data file
        try {
            Runtime.getRuntime().exec("/bin/rm -rf tmpDBFile");
            Runtime.getRuntime().exec("/bin/mkdir tmpDBFile");
            // Runtime.getRuntime().exec("/bin/rm -rf tmpDBFile/*.minibase-db tmpDBFile/*.minibase-log");
        } catch (IOException e) {
            System.err.println("IO error: " + e);
        }

        String[] arguments1 = {"batchnodeinsert", "NodeInsertDataPhase3.txt", "yhc"};
        BatchNodeInsertion.runBatchNodeInsertion(arguments1);

        String[] arguments2 = {"batchedgeinsert", "EdgeInsertDataPhase3.txt", "yhc"};
        BatchEdgeInsertion.runBatchEdgeInsertion(arguments2);

        String[] arguments3 = {"batchnodedelete", "NodeDeleteData.txt", "yhc"};
        // BatchNodeDeletion.runBatchNodeDeletion(arguments3); // have problem

        String[] arguments4 = {"batchedgedelete", "EdgeDeleteData.txt", "yhc"};
        // BatchEdgeDeletion.runBatchEdgeDeletion(arguments4);

        String[] arguments5 = {"nodequery", "yhc", "700", "1", "1"};
        // SimpleNodeQuery.runSimpleNodeQuery(arguments5);

        PathExpressionType1 test1 = new PathExpressionType1("yhc", "d:(1,1,1,1,1)/l:2", 1);
        test1.Query();
        test1.close();

        PathExpressionType2 test2 = new PathExpressionType2("yhc", "l:1/w:2/w:2/w:2", 3);
        test2.Query();
        test2.close();

        PathExpressionType3 test3 = new PathExpressionType3("yhc", "l:1/w:5", 1);
        test3.Query();
        test3.close();

        TriangleExpression test7 = new TriangleExpression("yhc", "w:3;w:3;w:3", 2);
        test7.Query();
        test7.close();

        /*
        while (true) {
            Scanner in = new Scanner(System.in);
            System.out.println("Please input your command (input \"help\" to learn supported operations):");
            String command = in.nextLine();
            String[] arguments = command.split(" ");
            if (arguments.length == 1 && arguments[0].equals("help")) {
                System.out.println("This graph database support following operations:");
                System.out.println("- batchnodeinsert NODEFILENAME GRAPHDBNAME");
                System.out.println("- batchedgeinsert EDGEFILENAME GRAPHDBNAME");
                System.out.println("- batchnodedelete NODEFILENAME GRAPHDBNAME");
                System.out.println("- batchedgedelete EDGEFILENAME GRAPHDBNAME");
                System.out.println("- nodequery GRAPHDBNAME NUMBUF QTYPE INDEX [QUERYOPTIONS]");
                System.out.println("- edgequery GRAPHDBNAME NUMBUF QTYPE INDEX [QUERYOPTIONS]");
                System.out.println();
            } else if (arguments.length == 1 && arguments[0].equals("exit")) {
                System.out.println("Gragh database clesed.");
                break;
            } else if (arguments.length == 3 && arguments[0].equals("batchnodeinsert")) {
                BatchNodeInsertion.runBatchNodeInsertion(arguments);
            } else if (arguments.length == 3 && arguments[0].equals("batchedgeinsert")) {
                BatchEdgeInsertion.runBatchEdgeInsertion(arguments);
            } else if (arguments.length == 3 && arguments[0].equals("batchnodedelete")) {
                BatchNodeDeletion.runBatchNodeDeletion(arguments);
            } else if (arguments.length == 3 && arguments[0].equals("batchedgedelete")) {
                BatchEdgeDeletion.runBatchEdgeDeletion(arguments);
            } else if (arguments.length == 5 && arguments[0].equals("nodequery")) {
                SimpleNodeQuery.runSimpleNodeQuery(arguments);
            } else if (arguments.length == 5 && arguments[0].equals("edgequery")) {
                SimpleEdgeQuery.runSimpleEdgeQuery(arguments);
            } else if (arguments.length == 2 && arguments[0].equals("PQ1a")) {
                PathExpressionType1.Query(arguments[1]);
            } else if (arguments.length == 2 && arguments[0].equals("PQ1b")) {

            } else if (arguments.length == 2 && arguments[0].equals("PQ1c")) {

            } else if (arguments.length == 2 && arguments[0].equals("PQ2a")) {

            } else if (arguments.length == 2 && arguments[0].equals("PQ2b")) {

            } else if (arguments.length == 2 && arguments[0].equals("PQ2c")) {

            } else if (arguments.length == 2 && arguments[0].equals("PQ3a")) {

            } else if (arguments.length == 2 && arguments[0].equals("PQ3b")) {

            } else if (arguments.length == 2 && arguments[0].equals("PQ3c")) {

            } else if (arguments.length == 2 && arguments[0].equals("TQa")) {

            } else if (arguments.length == 2 && arguments[0].equals("TQb")) {

            } else if (arguments.length == 2 && arguments[0].equals("TQc")) {
 
            } else {
                System.out.println("Invalid command! Please input again!");
                System.out.println();
            }
        }
        */

        // detete all data file
        try {
            Runtime.getRuntime().exec("/bin/rm -rf tmpDBFile");
            // Runtime.getRuntime().exec("/bin/mkdir ../../tmpDBFile");
            // Runtime.getRuntime().exec("/bin/rm -rf tmpDBFile/*.minibase-db tmpDBFile/*.minibase-log");
        } catch (IOException e) {
            System.err.println("IO error: " + e);
        }
    }
}