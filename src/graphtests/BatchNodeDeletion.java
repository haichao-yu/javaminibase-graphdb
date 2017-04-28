package graphtests;

import btree.*;
import diskmgr.PCounter;
import edgeheap.EdgeHeapfile;
import global.EID;
import global.GlobalConst;
import global.NID;
import global.SystemDefs;
import nodeheap.NodeHeapfile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yhc on 3/14/17.
 */
public class BatchNodeDeletion implements GlobalConst {

    private final static boolean OK = true;
    private final static boolean FAIL = false;

    public static void runBatchNodeDeletion(String[] arguments) {

        // arguments[0] = "batchnodedelete"
        // arguments[1] = "NODEFILENAME"
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
        File datafile = new File("../../testdata/" + dataFileName);
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

        // create btree to index node labels
        BTreeFile btf = Util.createBtreeFromStringKeyForNode(dbName, "btree_node_label", nhf);
        // create btree to index srcLabel of edges
        BTreeFile btfSrcLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_srcLabel", nhf, ehf, 1);
        // create btree to index dstLabel of edges
        BTreeFile btfDstLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_dstLabel", nhf, ehf, 2);

        // delete node
        if (status == OK) {
            Set<EID> outgoing = new HashSet<>();
            Set<EID> incoming = new HashSet<>();
            Set<EID> intersection = new HashSet<>();
            for (int i = 0; i < data.size(); i++) {
                BTFileScan btfScan = null;
                StringKey key = new StringKey(data.get(i));
                KeyDataEntry entry = null;
                NID delNID = null;
                EID delEID = null;
                try {
                    // delete node
                    btfScan = btf.new_scan(key, key);
                    entry = btfScan.get_next();
                    delNID = new NID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                    btfScan.DestroyBTreeFileScan(); // first destroy btfScan (unpin)
                    btf.Delete(key, delNID); // then delete node from btree (the operation order cannot be changed)
                    nhf.deleteNode(delNID);
                    // find edge whose source is this node
                    btfScan = btfSrcLabel.new_scan(key, key);
                    while (true) {
                        entry = btfScan.get_next();
                        if (entry != null) {
                            delEID = new EID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                            // btfScan.delete_current(); // we have problem here
                            // ehf.deleteEdge(delEID);
                            outgoing.add(delEID);
                        }
                        else {
                            break;
                        }
                    }
                    btfScan.DestroyBTreeFileScan();
                    // find edge whose destination is this node
                    btfScan = btfDstLabel.new_scan(key, key);
                    while (true) {
                        entry = btfScan.get_next();
                        if (entry != null) {
                            delEID = new EID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                            // btfScan.delete_current();
                            // ehf.deleteEdge(delEID);
                            incoming.add(delEID);
                        }
                        else {
                            break;
                        }
                    }
                    btfScan.DestroyBTreeFileScan();
                } catch (Exception e) {
                    e.printStackTrace();
                    status = FAIL;
                }
            }
            // find edge in both outgoing set and incoming set
            intersection.clear();
            intersection.addAll(outgoing);
            intersection.addAll(incoming);
            // delete edges in intersection
            for (EID eid : intersection) {
                try {
                    ehf.deleteEdge(eid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // destroy btree
        try {
            btfSrcLabel.destroyFile();
            btfDstLabel.destroyFile();
            btf.destroyFile();
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
            System.out.println("Batch nodes deletion completed successfully.\n");
        }

        gdb.close();
    }
}