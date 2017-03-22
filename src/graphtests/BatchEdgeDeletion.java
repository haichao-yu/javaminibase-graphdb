package graphtests;

import btree.*;
import diskmgr.PCounter;
import edgeheap.EdgeHeapfile;
import global.*;
import nodeheap.NodeHeapfile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yhc on 3/14/17.
 */
public class BatchEdgeDeletion implements GlobalConst {

    private final static boolean OK = true;
    private final static boolean FAIL = false;

    public static void runBatchEdgeDeletion(String[] arguments) {

        // arguments[0] = "batchedgedelete"
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

        // create btree to index srcLabel of edges
        BTreeFile btfSrcLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_srcLabel", nhf, ehf, 1);
        // create btree to index dstLabel of edges
        BTreeFile btfDstLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_dstLabel", nhf, ehf, 2);
        // create btree to index edgeLabel of edges
        BTreeFile btfEdgeLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_label", nhf, ehf, 3);

        // delete edge
        if (status == OK) {
            for (int i = 0; i < data.size(); i++) {
                String[] strs = data.get(i).split(" ");
                StringKey srcLabel = new StringKey(strs[0]);
                StringKey dstLabel = new StringKey(strs[1]);
                StringKey edgeLabel = new StringKey(strs[2]);
                BTFileScan btfScan = null;
                KeyDataEntry entry = null;
                EID eid = null;
                Set<EID> set1 = new HashSet<>();
                Set<EID> set2 = new HashSet<>();
                Set<EID> set3 = new HashSet<>();
                try {
                    // find RIDs according to srcLabel
                    btfScan = btfSrcLabel.new_scan(srcLabel, srcLabel);
                    while (true) {
                        entry = btfScan.get_next();
                        if (entry != null) {
                            eid = new EID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                            set1.add(eid);
                        }
                        else {
                            break;
                        }
                    }
                    btfScan.DestroyBTreeFileScan();
                    // find RIDs according to dstLabel
                    btfScan = btfDstLabel.new_scan(dstLabel, dstLabel);
                    while (true) {
                        entry = btfScan.get_next();
                        if (entry != null) {
                            eid = new EID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                            set2.add(eid);
                        }
                        else {
                            break;
                        }
                    }
                    btfScan.DestroyBTreeFileScan();
                    // find RIDs according to edgeLabel
                    btfScan = btfEdgeLabel.new_scan(edgeLabel, edgeLabel);
                    while (true) {
                        entry = btfScan.get_next();
                        if (entry != null) {
                            eid = new EID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                            set3.add(eid);
                        }
                        else  {
                            break;
                        }
                    }
                    btfScan.DestroyBTreeFileScan();
                    // find intersection of set1, set2 and set3
                    Set<EID> intersection = new HashSet<>();
                    intersection.clear();
                    intersection.addAll(set1);
                    intersection.retainAll(set2);
                    intersection.retainAll(set3);
                    // delete edges
                    for(EID delEID : intersection) {
                        ehf.deleteEdge(delEID);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    status = FAIL;
                }
            }
        }

        // destroy btree
        try {
            btfSrcLabel.destroyFile();
            btfDstLabel.destroyFile();
            btfEdgeLabel.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // print statistic information
        Util.printStatInfo(nhf, ehf);

        if (status == OK && SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                != SystemDefs.JavabaseBM.getNumBuffers()) {
            System.err.println("*** The heap-file scan has not unpinned " +
                    "its page after finishing\n");
            status = FAIL;
        }

        if (status == OK) {
            System.out.println("Batch edges deletion completed successfully.\n");
        }

        gdb.close();
    }
}