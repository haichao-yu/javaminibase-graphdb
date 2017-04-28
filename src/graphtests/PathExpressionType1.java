package graphtests;

import ZIndex.ZFile;
import btree.BTreeFile;
import diskmgr.PCounter;
import edgeheap.EdgeHeapfile;
import global.AttrOperator;
import global.AttrType;
import global.Descriptor;
import global.NID;
import heap.Tuple;
import iterator.*;
import nodeheap.NodeHeapfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yhc on 4/9/17.
 * NN = l:NN' | d:NN', NN' is the actual NN
 */

public class PathExpressionType1 {

    private GraphDBManager gdb;
    private String dbName;
    private String nodeHeapFileName;
    private String edgeHeapFileName;

    private NodeHeapfile nhf;
    private EdgeHeapfile ehf;

    private BTreeFile btfNodeLabel;
    private BTreeFile btfEdgeSrcLabel;
    private BTreeFile btfEdgeDstLabel;
    private ZFile zf;

    private String[] NN;
    private int type; // type=1 represents PQ1a, type=2 represents PQ1b, type=3 represents PQ1c

    /**
     * Constructor
     *
     * @param dbName
     * @param pathExpression
     */
    public PathExpressionType1(String dbName, String pathExpression, int type) {

        PCounter.initialize();
        // open db
        this.dbName = dbName;
        nodeHeapFileName = this.dbName + "_node";
        edgeHeapFileName = this.dbName + "_edge";
        gdb = new GraphDBManager();
        gdb.init(this.dbName);

        // open node heapfile
        try {
            nhf = new NodeHeapfile(nodeHeapFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // open edge heapfile
        try {
            ehf = new EdgeHeapfile(edgeHeapFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // build index
        btfNodeLabel = Util.createBtreeFromStringKeyForNode(dbName, "btree_node_label", nhf);
        btfEdgeSrcLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_srcLabel", nhf, ehf, 1);
        btfEdgeDstLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_dstLabel", nhf, ehf, 2);
        zf = Util.createZIndexFromZValueForNode(dbName, "zindex", nhf);

        // get NNs
        NN = pathExpression.split("/");

        // get type
        this.type = type;
    }

    /**
     * Task 2.3
     *
     * @param nid
     * @return
     */
    private Iterator Operator(NID nid) {

        // create FileScan for node
        FileScan nodeScan = (FileScan) Util.createFileScanForNode(nodeHeapFileName);

        // Initialize joinExpres
        CondExpr[] joinExprs = new CondExpr[2];
        joinExprs[0] = new CondExpr();
        joinExprs[1] = null; //used for ending the while loop

        // Join Operations
        Iterator[] inlj1 = new Iterator[NN.length];
        Iterator[] inlj2 = new Iterator[NN.length];
        for (int i = 1; i < NN.length; i++) {

            // get edge iterator
            if (i == 1) {
                // src node id = target nid
                joinExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
                joinExprs[0].next = null;
                joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
                joinExprs[0].type2 = new AttrType(AttrType.attrNID);
                joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 1);
                joinExprs[0].operand2.nid = nid;

                try {
                    inlj1[i] = new NewIndexNestedLoopJoinsNodeEdge(ehf, btfEdgeSrcLabel, btfEdgeDstLabel, nodeScan, joinExprs, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else { // i > 1
                try {
                    inlj1[i] = new NewIndexNestedLoopJoinsNodeEdge(ehf, btfEdgeSrcLabel, btfEdgeDstLabel, inlj2[i - 1], null, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // get node iterator under node condition
            String targetNN = NN[i].substring(2);
            if(NN[i].startsWith("d:")) {
                if (targetNN.charAt(0) == '(' && targetNN.charAt(targetNN.length() - 1) == ')') { // target NN is node descriptor
                    String strDesc = targetNN.substring(1, targetNN.length() - 1);
                    String[] dimensions = strDesc.split(",");
                    Descriptor targetDescriptor = new Descriptor(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]), Integer.parseInt(dimensions[2]), Integer.parseInt(dimensions[3]), Integer.parseInt(dimensions[4]));

                    joinExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
                    joinExprs[0].next = null;
                    joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
                    joinExprs[0].type2 = new AttrType(AttrType.attrDesc);
                    joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 2);
                    joinExprs[0].operand2.desc = targetDescriptor;
                }
            }else { // target NN is node label
                String targeLabel = targetNN;

                joinExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
                joinExprs[0].next = null;
                joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
                joinExprs[0].type2 = new AttrType(AttrType.attrString);
                joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 1);
                joinExprs[0].operand2.string = targeLabel;
            }
            try {
                inlj2[i] = new NewIndexNestedLoopJoinsEdgeNode(nhf, btfNodeLabel, inlj1[i], joinExprs, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return inlj2[NN.length - 1];
    }

    /**
     * Task 2.6
     */
    public void Query() {

        ArrayList<Path> result = new ArrayList<>();

        // get NID from first NN
        String firstNN = NN[0];
        ArrayList<NID> targetNIDs = Util.getNIDsFromFirstNN(firstNN, zf, btfNodeLabel);

        // for every NID, do Operator(NID)
        for (NID nid : targetNIDs) {

            String head = null;
            try {
                head = nhf.getNode(nid).getLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Iterator tails = Operator(nid);
            Tuple tuple = null;
            try {
                while ((tuple = tails.get_next()) != null) {
                    String tail = tuple.getStrFld(1);
                    result.add(new Path(head, tail));
                }
            } catch (Exception e) {
                System.err.println("" + e);
                e.printStackTrace();
                Runtime.getRuntime().exit(1);
            }
            try {
                tails.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.type == 1) { // PQ1a: output directly
            for (Path path : result) {
                System.out.println(path.toString());
            }
            System.out.println("Total number of paths: " + result.size());
        } else if (this.type == 2) { // PQ1b: sort the results in the labels of head and tail labels, and output
            Collections.sort(result);
            for (Path path : result) {
                System.out.println(path.toString());
            }
            System.out.println("Total number of paths: " + result.size());
        } else { // PQ1c: output only distinct head/tail node label pairs
            Set<Path> set = new HashSet<>();
            set.addAll(result);
            for (Path path : set) {
                System.out.println(path.toString());
            }
            System.out.println("Total number of paths: " + set.size());
        }

        // print query plan
        System.out.println("Plan used (from current node to next node through NN):\n" +
                "Pi(Node) ((Pi(Edge) (Node |X| (source, edge_condition=null) Edge)) |X| (destination, node_condition=NN) Node)");

        // print statistic information
        Util.printStatInfo(nhf, ehf);
        System.out.println();
    }

    /**
     * close the operator
     */
    public void close() {
        try {
            btfNodeLabel.destroyFile();
            btfEdgeSrcLabel.destroyFile();
            btfEdgeDstLabel.destroyFile();
            zf.destroyFile();
            gdb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}