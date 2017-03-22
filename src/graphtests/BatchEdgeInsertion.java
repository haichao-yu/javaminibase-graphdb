package graphtests;

import btree.*;
import diskmgr.PCounter;
import edgeheap.Edge;
import edgeheap.EdgeHeapfile;
import global.GlobalConst;
import global.NID;
import global.SystemDefs;
import nodeheap.NodeHeapfile;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by yhc on 3/14/17.
 */
public class BatchEdgeInsertion implements GlobalConst {

    private final static boolean OK = true;
    private final static boolean FAIL = false;

    public static void runBatchEdgeInsertion(String[] arguments) {

        // arguments[0] = "batchedgeinsert"
        // arguments[1] = "EDGEFILENAME"
        // arguments[2] = "GRAPHDBNAME"

        PCounter.initialize();

        boolean status = OK;

        String dataFileName = arguments[1];
        String dbName = arguments[2];
        GraphDBManager gdb = new GraphDBManager();
        gdb.init(dbName);

        String nodeHeapFileName = dbName + "_node";
        String edgeHeapFileName = dbName + "_edge";

        // read data from test data file
        File datafile = new File("testdata/" + dataFileName);
        ArrayList<String> data = Util.readDataFromFile(datafile);

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

        // create btree to index node labels
        BTreeFile btf = Util.createBtreeFromStringKeyForNode(dbName, "btree_node_label", nhf);

        // create or open edge heapfile
        EdgeHeapfile ehf = null;
        try {
            ehf = new EdgeHeapfile(edgeHeapFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // insert edges
        if (status == OK) {
            // traverse edge data
            for (int i = 0; i < data.size(); i++) {
                String[] strs = data.get(i).split(" ");
                String srcLabel = strs[0];
                String dstLabel = strs[1];
                String edgeLabel = strs[2];
                int weight = Integer.parseInt(strs[3]);
                // find nid according to node label
                BTFileScan btfScan = null;
                KeyDataEntry entry = null;
                StringKey key = null;
                NID srcNID = null;
                NID dstNID = null;
                try {
                    // find source NID
                    key = new StringKey(srcLabel);
                    btfScan = btf.new_scan(key, key);
                    entry = btfScan.get_next();
                    srcNID = new NID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                    btfScan.DestroyBTreeFileScan();
                    // find destination NID
                    key = new StringKey(dstLabel);
                    btfScan = btf.new_scan(key, key);
                    entry = btfScan.get_next();
                    dstNID = new NID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                    btfScan.DestroyBTreeFileScan();
                    // insertion
                    Edge edge = new Edge();
                    edge.setSource(srcNID);
                    edge.setDestination(dstNID);
                    edge.setLabel(edgeLabel);
                    edge.setWeight(weight);
                    ehf.insertEdge(edge.getEdgeByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                    status = FAIL;
                }
            }
        }

        // destroy btree
        try {
            btf.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // print statistic information
        Util.printStatInfo(nhf, ehf);

        if (status == OK && SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                != SystemDefs.JavabaseBM.getNumBuffers()) {
            System.err.println("*** There are left pages pinned\n");
            status = FAIL;
        }

        if (status == OK) {
            System.out.println("Batch edges insertion completed successfully.\n");
        }

        gdb.close();
    }
}
