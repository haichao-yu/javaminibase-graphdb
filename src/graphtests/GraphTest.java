package graphtests;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by yhc on 3/14/17.
 */
public class GraphTest {


    public static void main(String[] args) {

        // detete all data file
        try {
            Runtime.getRuntime().exec("/bin/rm -rf ../../tmpDBFile");
            Runtime.getRuntime().exec("/bin/mkdir ../../tmpDBFile");
            // Runtime.getRuntime().exec("/bin/rm -rf tmpDBFile/*.minibase-db tmpDBFile/*.minibase-log");
        } catch (IOException e) {
            System.err.println("IO error: " + e);
        }

        /*
        String[] arguments1 = {"batchnodeinsert", "Phase3MyNodeInsertData.txt", "yhc"};
        BatchNodeInsertion.runBatchNodeInsertion(arguments1);

        String[] arguments2 = {"batchedgeinsert", "Phase3MyEdgeInsertData.txt", "yhc"};
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

        PathExpressionType3 test3 = new PathExpressionType3("yhc", "l:1//w:5", 1);
        test3.Query();
        test3.close();

        TriangleExpression test7 = new TriangleExpression("yhc", "w:3;w:3;w:3", 2);
        test7.Query();
        test7.close();
        */

        while (true) {
            Scanner in = new Scanner(System.in);
            System.out.println("Please input your command (input \"help\" to learn supported operations, input \"exit\" to exit):");
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
                System.out.println("- PQ1a GRAPHDBNAME PATH_EXPRESSION (NN/.../NN)");
                System.out.println("- PQ1b GRAPHDBNAME PATH_EXPRESSION (NN/.../NN)");
                System.out.println("- PQ1c GRAPHDBNAME PATH_EXPRESSION (NN/.../NN)");
                System.out.println("- PQ2a GRAPHDBNAME PATH_EXPRESSION (NN/EN/.../EN)");
                System.out.println("- PQ2b GRAPHDBNAME PATH_EXPRESSION (NN/EN/.../EN)");
                System.out.println("- PQ2c GRAPHDBNAME PATH_EXPRESSION (NN/EN/.../EN)");
                System.out.println("- PQ3a GRAPHDBNAME PATH_EXPRESSION (NN//Bound)");
                System.out.println("- PQ3b GRAPHDBNAME PATH_EXPRESSION (NN//Bound)");
                System.out.println("- PQ3c GRAPHDBNAME PATH_EXPRESSION (NN//Bound)");
                System.out.println("- TQa GRAPHDBNAME TRIANGLE_EXPRESSION (EN;EN;EN)");
                System.out.println("- TQb GRAPHDBNAME TRIANGLE_EXPRESSION (EN;EN;EN)");
                System.out.println("- TQc GRAPHDBNAME TRIANGLE_EXPRESSION (EN;EN;EN)");
                System.out.println();
            } else if (arguments.length == 1 && arguments[0].equals("exit")) {
                System.out.println("Graph database closed.");
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
            } else if (arguments.length == 3 && arguments[0].equals("PQ1a")) {
                PathExpressionType1 pq1a = new PathExpressionType1(arguments[1], arguments[2], 1);
                pq1a.Query();
                pq1a.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ1b")) {
                PathExpressionType1 pq1b = new PathExpressionType1(arguments[1], arguments[2], 2);
                pq1b.Query();
                pq1b.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ1c")) {
                PathExpressionType1 pq1c = new PathExpressionType1(arguments[1], arguments[2], 3);
                pq1c.Query();
                pq1c.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ2a")) {
                PathExpressionType2 pq2a = new PathExpressionType2(arguments[1], arguments[2], 1);
                pq2a.Query();
                pq2a.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ2b")) {
                PathExpressionType2 pq2b = new PathExpressionType2(arguments[1], arguments[2], 2);
                pq2b.Query();
                pq2b.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ2c")) {
                PathExpressionType2 pq2c = new PathExpressionType2(arguments[1], arguments[2], 3);
                pq2c.Query();
                pq2c.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ3a")) {
                PathExpressionType3 pq3a = new PathExpressionType3(arguments[1], arguments[2], 1);
                pq3a.Query();
                pq3a.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ3b")) {
                PathExpressionType3 pq3b = new PathExpressionType3(arguments[1], arguments[2], 2);
                pq3b.Query();
                pq3b.close();
            } else if (arguments.length == 3 && arguments[0].equals("PQ3c")) {
                PathExpressionType3 pq3c = new PathExpressionType3(arguments[1], arguments[2], 3);
                pq3c.Query();
                pq3c.close();
            } else if (arguments.length == 3 && arguments[0].equals("TQa")) {
                TriangleExpression tqa = new TriangleExpression(arguments[1], arguments[2], 1);
                tqa.Query();
                tqa.close();
            } else if (arguments.length == 3 && arguments[0].equals("TQb")) {
                TriangleExpression tqb = new TriangleExpression(arguments[1], arguments[2], 2);
                tqb.Query();
                tqb.close();
            } else if (arguments.length == 3 && arguments[0].equals("TQc")) {
                TriangleExpression tqc = new TriangleExpression(arguments[1], arguments[2], 3);
                tqc.Query();
                tqc.close();
            } else {
                System.out.println("Invalid command! Please input again!");
                System.out.println();
            }
        }

        // detete all data file
        try {
            Runtime.getRuntime().exec("/bin/rm -rf ../../tmpDBFile");
            // Runtime.getRuntime().exec("/bin/mkdir ../../tmpDBFile");
            // Runtime.getRuntime().exec("/bin/rm -rf tmpDBFile/*.minibase-db tmpDBFile/*.minibase-log");
        } catch (IOException e) {
            System.err.println("IO error: " + e);
        }
    }
}