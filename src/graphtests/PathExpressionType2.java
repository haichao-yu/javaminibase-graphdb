package graphtests;

import ZIndex.ZFile;
import btree.*;
import diskmgr.PCounter;
import edgeheap.EdgeHeapfile;
import global.AttrOperator;
import global.AttrType;
import global.NID;
import global.TupleOrder;
import heap.Tuple;
import iterator.*;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.util.ArrayList;

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
        FileScan nodeScan = (FileScan) Util.createFileScanForNode(nodeHeapFileName, null);

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

        /* for create sort iterator and duplicate eliminate iterator */
        AttrType[] attrTypes = new AttrType[2]; // fields attribute types
        attrTypes[0] = new AttrType(AttrType.attrString);
        attrTypes[1] = new AttrType(AttrType.attrDesc);

        short[] stringSizes = new short[1]; // size of string field in node
        stringSizes[0] = (short) Node.max_length_of_node_label;

        TupleOrder[] order = new TupleOrder[2];
        order[0] = new TupleOrder(TupleOrder.Ascending);
        order[1] = new TupleOrder(TupleOrder.Descending);

        FldSpec[] projList = new FldSpec[2]; //output - node format
        RelSpec rel = new RelSpec(RelSpec.outer);
        projList[0] = new FldSpec(rel, 1);
        projList[1] = new FldSpec(rel, 2);

        int sortField = 1; // sort on node label
        int sortFieldLength = Node.max_length_of_node_label;
        int SORTPGNUM = 13;
        /* --------------------------------------------------------- */

        ArrayList<Path> result = new ArrayList<>();

        String firstNN = EN[0]; // EN[0] is a NN
        Iterator heads = Util.getFirstNodesFromFirstNN(firstNN, nodeHeapFileName);
        if (this.type == 2) { // sort
            try {
                heads = new Sort(attrTypes, (short) attrTypes.length, stringSizes, heads, sortField, order[0], sortFieldLength, SORTPGNUM);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.type == 3) { // duplicate eliminate
            try {
                heads = new DuplElim(attrTypes, (short) 2, stringSizes, heads, 10, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Tuple head = null;
        try {
            while ((head = heads.get_next()) != null) {
                String head_label = head.getStrFld(1); // node label is unique
                BTFileScan btfScan = btfNodeLabel.new_scan(new StringKey(head_label), new StringKey(head_label));
                KeyDataEntry entry = btfScan.get_next();
                NID nid = new NID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                Iterator tails = Operator(nid);
                if (this.type == 2) { // sort
                    try {
                        tails = new Sort(attrTypes, (short) attrTypes.length, stringSizes, tails, sortField, order[0], sortFieldLength, SORTPGNUM);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (this.type == 3) { // duplicate eliminate
                    try {
                        tails = new DuplElim(attrTypes, (short) 2, stringSizes, tails, 10, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Tuple tail = null;
                try {
                    while ((tail = tails.get_next()) != null) {
                        String tail_label = tail.getStrFld(1);
                        result.add(new Path(head_label, tail_label));
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
                btfScan.DestroyBTreeFileScan();
            }
        } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }
        try {
            heads.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Path path : result) {
            System.out.println(path.toString());
        }
        System.out.println("Total number of paths: " + result.size());

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