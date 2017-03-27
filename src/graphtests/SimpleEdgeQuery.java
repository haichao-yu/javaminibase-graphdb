package graphtests;

import btree.*;
import diskmgr.PCounter;
import edgeheap.EScan;
import edgeheap.Edge;
import edgeheap.EdgeHeapfile;
import global.*;
import heap.Tuple;
import nodeheap.NScan;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.util.Scanner;

/**
 * Created by yhc on 3/15/17.
 */
public class SimpleEdgeQuery implements GlobalConst{

    private final static boolean OK = true;
    private final static boolean FAIL = false;

    public static void runSimpleEdgeQuery(String[] arguments) {

        // arguments[0] = "edgequery"
        // arguments[1] = "GRAPHDBNAME"
        // arguments[2] = "NUMBUF"
        // arguments[3] = "QTYPE"
        // arguments[4] = "INDEX"

        PCounter.initialize();

        boolean status = OK;

        String dbName = arguments[1];
        int numBuf = Integer.parseInt(arguments[2]);
        int qtype = Integer.parseInt(arguments[3]);
        int index = Integer.parseInt(arguments[4]);
        GraphDBManager gdb = new GraphDBManager();
        gdb.init(dbName, numBuf);

        String nodeHeapFileName = dbName + "_node";
        String edgeHeapFileName = dbName + "_edge";

        // create or open node heapfile
        NodeHeapfile nhf = null;
        try {
            nhf = new NodeHeapfile(nodeHeapFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (status == OK && SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                != SystemDefs.JavabaseBM.getNumBuffers()) {
            System.err.println("*** The heap file has left pages pinned\n");
            status = FAIL;
        }

        // create or open edge heapfile
        EdgeHeapfile ehf = null;
        try {
            ehf = new EdgeHeapfile(edgeHeapFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (status == OK && SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                != SystemDefs.JavabaseBM.getNumBuffers()) {
            System.err.println("*** The heap file has left pages pinned\n");
            status = FAIL;
        }

        switch (qtype) {
            case 0:
                queryType0(nhf, ehf);
                break;
            case 1:
                queryType1(dbName, nhf, ehf);
                break;
            case 2:
                queryType2(dbName, nhf, ehf);
                break;
            case 3:
                queryType3(dbName, nhf, ehf);
                break;
            case 4:
                queryType4(dbName, nhf, ehf);
                break;
            case 5:
                queryType5(dbName, nhf, ehf);
                break;
            case 6:
                queryType6();
                break;
            default:
                System.out.println("Node query does not support QTYPE " + qtype + ". QTYPE should be 0-5. Please input again.");
                break;
        }

        // print statistic information
        Util.printStatInfo(nhf, ehf);

        gdb.close();
    }

    /**
     *
     * @param nhf
     * @param ehf
     */
    private static void queryType0(NodeHeapfile nhf, EdgeHeapfile ehf) {

        System.out.println("Print the edge data in the order it occurs in node heap:");

        // create edge scan
        EScan eScan = null;
        try {
            eScan = ehf.openScan();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // scan and print edge data in the order it occurs in the node heap
        Tuple tuple;
        Edge edge;
        EID eid = new EID();
        boolean done = false;
        while (!done) {
            try {
                tuple = eScan.getNext(eid);
                if (tuple == null) {
                    done = true;
                    break;
                }
                if (!done) {
                    edge = new Edge(tuple.data, 0);
                    NID srcNID = edge.getSource();
                    NID dstNID = edge.getDestination();
                    System.out.println("[" + nhf.getNode(srcNID).getLabel() + ", " + nhf.getNode(dstNID).getLabel() + ", " + edge.getLabel() + ", " + edge.getWeight() + "]");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Query completed.\n");
    }

    /**
     *
     * @param dbName
     * @param nhf
     * @param ehf
     */
    private static void queryType1(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf) {

        System.out.println("Print the edge datain increasing alphanumerical order of source labels: ");

        // create btree to index srcLabel of edges
        BTreeFile btfSrcLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_srcLabel", nhf, ehf, 1);

        BTFileScan btfScan = null;
        KeyDataEntry entry = null;
        EID eid = null;
        Edge edge = null;
        try {
            btfScan = btfSrcLabel.new_scan(null, null);
            while (true) {
                entry = btfScan.get_next();
                if (entry != null) {
                    eid = new EID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                    edge = ehf.getEdge(eid);
                    NID srcNID = edge.getSource();
                    NID dstNID = edge.getDestination();
                    System.out.println("[" + nhf.getNode(srcNID).getLabel() + ", " + nhf.getNode(dstNID).getLabel() + ", " + edge.getLabel() + ", " + edge.getWeight() + "]");
                }
                else {
                    break;
                }
            }
            btfScan.DestroyBTreeFileScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            btfSrcLabel.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Query completed.\n");
    }

    private static void queryType2(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf) {

        System.out.println("Print the edge datain increasing alphanumerical order of destination labels: ");

        // create btree to index dstLabel of edges
        BTreeFile btfDstLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_srcLabel", nhf, ehf, 2);

        BTFileScan btfScan = null;
        KeyDataEntry entry = null;
        EID eid = null;
        Edge edge = null;
        try {
            btfScan = btfDstLabel.new_scan(null, null);
            while (true) {
                entry = btfScan.get_next();
                if (entry != null) {
                    eid = new EID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                    edge = ehf.getEdge(eid);
                    NID srcNID = edge.getSource();
                    NID dstNID = edge.getDestination();
                    System.out.println("[" + nhf.getNode(srcNID).getLabel() + ", " + nhf.getNode(dstNID).getLabel() + ", " + edge.getLabel() + ", " + edge.getWeight() + "]");
                }
                else {
                    break;
                }
            }
            btfScan.DestroyBTreeFileScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            btfDstLabel.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Query completed.\n");
    }

    private static void queryType3(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf) {

        System.out.println("Print the edge datain increasing alphanumerical order of edge labels: ");

        // create btree to index dstLabel of edges
        BTreeFile btfEdgeLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_srcLabel", nhf, ehf, 3);

        BTFileScan btfScan = null;
        KeyDataEntry entry = null;
        EID eid = null;
        Edge edge = null;
        try {
            btfScan = btfEdgeLabel.new_scan(null, null);
            while (true) {
                entry = btfScan.get_next();
                if (entry != null) {
                    eid = new EID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                    edge = ehf.getEdge(eid);
                    NID srcNID = edge.getSource();
                    NID dstNID = edge.getDestination();
                    System.out.println("[" + nhf.getNode(srcNID).getLabel() + ", " + nhf.getNode(dstNID).getLabel() + ", " + edge.getLabel() + ", " + edge.getWeight() + "]");
                }
                else {
                    break;
                }
            }
            btfScan.DestroyBTreeFileScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            btfEdgeLabel.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Query completed.\n");
    }

    private static void queryType4(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf) {

        System.out.println("Print the edge datain increasing alphanumerical order of weights: ");

        // create btree
        BTreeFile btfWeight = Util.createBtreeFromWeightForEdge(dbName, "btree_edge_weight", nhf, ehf);

        BTFileScan btfScan = null;
        KeyDataEntry entry = null;
        EID eid = null;
        Edge edge = null;
        try {
            btfScan = btfWeight.new_scan(null, null);
            while (true) {
                entry = btfScan.get_next();
                if (entry != null) {
                    eid = new EID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                    edge = ehf.getEdge(eid);
                    NID srcNID = edge.getSource();
                    NID dstNID = edge.getDestination();
                    System.out.println("[" + nhf.getNode(srcNID).getLabel() + ", " + nhf.getNode(dstNID).getLabel() + ", " + edge.getLabel() + ", " + edge.getWeight() + "]");
                }
                else {
                    break;
                }
            }
            btfScan.DestroyBTreeFileScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            btfWeight.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Query completed.\n");
    }

    private static void queryType5(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf) {

        // get lower and upper bound on edge weights
        Scanner in = new Scanner(System.in);
        System.out.println("Please input a lower bound for edge weights:");
        int lower = in.nextInt();
        System.out.println("Please input a upper bound for edge weights:");
        int upper = in.nextInt();

        // create btree
        BTreeFile btfWeight = Util.createBtreeFromWeightForEdge(dbName, "btree_edge_weight", nhf, ehf);

        BTFileScan btfScan = null;
        KeyDataEntry entry = null;
        EID eid = null;
        Edge edge = null;
        try {
            btfScan = btfWeight.new_scan(new IntegerKey(lower), new IntegerKey(upper));
            while (true) {
                entry = btfScan.get_next();
                if (entry != null) {
                    eid = new EID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                    edge = ehf.getEdge(eid);
                    NID srcNID = edge.getSource();
                    NID dstNID = edge.getDestination();
                    System.out.println("[" + nhf.getNode(srcNID).getLabel() + ", " + nhf.getNode(dstNID).getLabel() + ", " + edge.getLabel() + ", " + edge.getWeight() + "]");
                }
                else {
                    break;
                }
            }
            btfScan.DestroyBTreeFileScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            btfWeight.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Query completed.\n");
    }

    private static void queryType6() {

        System.out.println("Query completed.\n");
    }
}
