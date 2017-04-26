package graphtests;

import ZIndex.ZFile;
import btree.BTreeFile;
import diskmgr.PCounter;
import edgeheap.EdgeHeapfile;
import global.AttrOperator;
import global.AttrType;
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
 */

public class PathExpressionType2 {

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

    private String[] EN;
    private int type; // type=1 represents PQ1a, type=2 represents PQ1b, type=3 represents PQ1c

    /**
     * Constructor
     *
     * @param dbName
     * @param pathExpression
     * @param type
     */
    public PathExpressionType2(String dbName, String pathExpression, int type) {

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
        EN = pathExpression.split("/");

        // get type
        this.type = type;
    }

    /**
     * Task 2.4
     *
     * @param nid
     * @return
     */
    public Iterator Operator(NID nid) {

        // create FileScan for node
        FileScan nodeScan = (FileScan) Util.createFileScanForNode(nodeHeapFileName);

        // Initialize joinExpres
        CondExpr[] joinExprs = new CondExpr[3];

        // Join Operations
        Iterator[] inlj1 = new Iterator[EN.length];
        Iterator[] inlj2 = new Iterator[EN.length];
        inlj2[0] = nodeScan;
        for (int i = 1; i < EN.length; i++) {

            // set edge condition
            String targetEN = EN[i].substring(2);
            if (ENIsLabelOrWeight(EN[i])) { // target EN is edge label
                String targetLabel = targetEN;

                joinExprs[0] = new CondExpr();
                joinExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
                joinExprs[0].next = null;
                joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
                joinExprs[0].type2 = new AttrType(AttrType.attrString);
                joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 3);
                joinExprs[0].operand2.string = targetLabel;
                joinExprs[1] = null;
            } else { // target EN is max edge weight
                int maxEdgeWeight = Integer.parseInt(targetEN);

                joinExprs[0] = new CondExpr();
                joinExprs[0].op = new AttrOperator(AttrOperator.aopLE);
                joinExprs[0].next = null;
                joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
                joinExprs[0].type2 = new AttrType(AttrType.attrInteger);
                joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 4);
                joinExprs[0].operand2.integer = maxEdgeWeight;
                joinExprs[1] = null;
            }

            // when i == 1, add a edge condition that srcNID == givenNID
            if (i == 1) {
                // src node id = target nid
                joinExprs[1] = new CondExpr();
                joinExprs[1].op = new AttrOperator(AttrOperator.aopEQ);
                joinExprs[1].next = null;
                joinExprs[1].type1 = new AttrType(AttrType.attrSymbol);
                joinExprs[1].type2 = new AttrType(AttrType.attrNID);
                joinExprs[1].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 1);
                joinExprs[1].operand2.nid = nid;
                joinExprs[2] = null;
            }

            try {
                // Node |X| (source, edge_condition) Edge;  Project to Edge
                inlj1[i] = new NewIndexNestedLoopJoinsNodeEdge(ehf, btfEdgeSrcLabel, btfEdgeDstLabel, inlj2[i - 1], joinExprs, 1);
                // Edge |X| (destination, node_condition) Node;  Project to Node
                inlj2[i] = new NewIndexNestedLoopJoinsEdgeNode(nhf, btfNodeLabel, inlj1[i], null, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return inlj2[EN.length - 1];
    }

    /**
     * Task 2.7
     */
    public void Query() {

        ArrayList<Path> result = new ArrayList<>();

        // get NID from first NN
        String firstNN = EN[0]; // EN[0] is a NN
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

    /**
     * @param str
     * @return true if EN is label; false if EN is weight
     */
    private boolean ENIsLabelOrWeight(String str) {
        if(str.startsWith("l:"))
            return true;
        return false;
    }
}