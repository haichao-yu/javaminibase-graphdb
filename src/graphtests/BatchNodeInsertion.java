package graphtests;

import diskmgr.PCounter;
import edgeheap.EdgeHeapfile;
import global.Descriptor;
import global.GlobalConst;
import global.SystemDefs;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by yhc on 3/14/17.
 */
public class BatchNodeInsertion implements GlobalConst {

    private final static boolean OK = true;
    private final static boolean FAIL = false;

    public static void runBatchNodeInsertion(String[] arguments) {

        // arguments[0] = "batchnodeinsert"
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

        // insert data to node heapfile
        if (status == OK) {
            for (int i = 0; i < data.size(); i++) {
                String[] strs = data.get(i).split(" ");
                String label = strs[0];
                Descriptor desc = new Descriptor(Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3]), Integer.parseInt(strs[4]), Integer.parseInt(strs[5]));
                Node node = new Node();
                try {
                    node.setLabel(strs[0]);
                    node.setDesc(desc);
                    nhf.insertNode(node.getNodeByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (status == OK && SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                    != SystemDefs.JavabaseBM.getNumBuffers()) {

                System.err.println("*** Insertion left a page pinned\n");
                status = FAIL;
            }
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

        // print statistic information
        Util.printStatInfo(nhf, ehf);

        if (status == OK && SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                != SystemDefs.JavabaseBM.getNumBuffers()) {
            System.err.println("*** There are left pages pinned\n");
            status = FAIL;
        }

        if (status == OK) {
            System.out.println("Batch nodes insertion completed successfully.\n");
        }

        gdb.close();
    }
}