package graphtests;

import ZIndex.DescriptorKey;
import ZIndex.ZFile;
import btree.*;
import diskmgr.PCounter;
import edgeheap.EScan;
import edgeheap.Edge;
import edgeheap.EdgeHeapfile;
import global.*;
import heap.Tuple;
import iterator.*;
import nodeheap.NScan;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by yhc on 3/15/17.
 */
public class Util {

    private final static boolean OK = true;
    private final static boolean FAIL = false;

    /**
     * read data from test data file
     * @param datafile
     * @return
     */
    public static ArrayList<String> readDataFromFile(File datafile) {

        ArrayList<String> data = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(datafile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * create btree from string key for node (node label)
     * @param dbName
     * @param nameOfBtree
     * @param nhf
     * @return
     */
    public static BTreeFile createBtreeFromStringKeyForNode(String dbName, String nameOfBtree, NodeHeapfile nhf) {

        boolean status = OK;

        // create node scan
        NScan nScan = null;
        if (status == OK) {
            try {
                nScan = nhf.openScan();
            } catch (Exception e) {
                status = FAIL;
                System.err.println("*** Error opening scan\n");
                e.printStackTrace();
            }
        }

        // create btree to index node labels
        BTreeFile btf = null;
        if (status == OK) {
            try {
                btf = new BTreeFile(dbName + nameOfBtree, AttrType.attrString, 20, 1);
                // BT.printAllLeafPages(btf.getHeaderPage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tuple tuple = new Tuple();
            Node node;
            NID nid = new NID();
            boolean done = false;
            while (!done) {
                try {
                    tuple = nScan.getNext(nid);
                    if (tuple == null) {
                        done = true;
                        break;
                    }
                } catch (Exception e) {
                    status = FAIL;
                    e.printStackTrace();
                }
                if (status == OK && !done) {
                    node = new Node(tuple.data, 0);
                    try {
                        KeyClass key = new StringKey(node.getLabel());
                        btf.insert(key, nid); // insertion (yhc)
                        // System.out.println("(" + nid.pageNo.pid + "," + nid.slotNo + ")   " + node.getDesc().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // close node scan
        nScan.closescan();

        return btf;
    }

    /**
     * create btree from string key for edge (source node label, destination node label, edge label)
     * @param dbName
     * @param nameOfBtree
     * @param ehf
     * @param type type=1: index srcLabel; type=2: index dstLabel; type=3: index edgeLabel
     * @return
     */
    public static BTreeFile createBtreeFromStringKeyForEdge(String dbName, String nameOfBtree, NodeHeapfile nhf, EdgeHeapfile ehf, int type) {

        boolean status = OK;

        // create edge scan
        EScan eScan = null;
        if (status == OK) {
            try {
                eScan = ehf.openScan();
            } catch (Exception e) {
                status = FAIL;
                System.err.println("*** Error opening scan\n");
                e.printStackTrace();
            }
        }

        // create btree for edges
        BTreeFile btf = null;
        if (status == OK) {
            try {
                btf = new BTreeFile(dbName + nameOfBtree, AttrType.attrString, 20, 1);
                // BT.printAllLeafPages(btf.getHeaderPage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tuple tuple = new Tuple();
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
                } catch (Exception e) {
                    status = FAIL;
                    e.printStackTrace();
                }
                if (status == OK && !done) {
                    edge = new Edge(tuple.data, 0);
                    try {
                        switch (type) {
                            case 1:
                                Node srcNode = nhf.getNode(edge.getSource());
                                String srcLabel = srcNode.getLabel();
                                KeyClass keySrc = new StringKey(srcLabel);
                                btf.insert(keySrc, eid);
                                break;
                            case 2:
                                Node dstNode = nhf.getNode(edge.getDestination());
                                String dstLabel = dstNode.getLabel();
                                KeyClass keyDst = new StringKey(dstLabel);
                                btf.insert(keyDst, eid);
                                break;
                            case 3:
                                String edgeLabel = edge.getLabel();
                                KeyClass keyEdge = new StringKey(edgeLabel);
                                btf.insert(keyEdge, eid);
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // close edge scan
        eScan.closescan();

        return btf;
    }

    /**
     * create btree from edge weight
     * @param dbName
     * @param nameOfBtree
     * @param nhf
     * @param ehf
     * @return
     */
    public static BTreeFile createBtreeFromWeightForEdge(String dbName, String nameOfBtree, NodeHeapfile nhf, EdgeHeapfile ehf) {

        boolean status = OK;

        // create edge scan
        EScan eScan = null;
        if (status == OK) {
            try {
                eScan = ehf.openScan();
            } catch (Exception e) {
                status = FAIL;
                System.err.println("*** Error opening scan\n");
                e.printStackTrace();
            }
        }

        // create btree for edges
        BTreeFile btf = null;
        if (status == OK) {
            try {
                btf = new BTreeFile(dbName + nameOfBtree, AttrType.attrInteger, 20, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tuple tuple = new Tuple();
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
                } catch (Exception e) {
                    status = FAIL;
                    e.printStackTrace();
                }
                if (status == OK && !done) {
                    edge = new Edge(tuple.data, 0);
                    try {
                        int weight = edge.getWeight();
                        KeyClass keyWeight = new IntegerKey(weight);
                        btf.insert(keyWeight, eid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // close edge scan
        eScan.closescan();

        return btf;
    }

    /**
     * Create Z Index from descriptor
     * @param dbName
     * @param nameOfZIndex
     * @param nhf
     * @return
     */
    public static ZFile createZIndexFromZValueForNode(String dbName, String nameOfZIndex, NodeHeapfile nhf) {

        boolean status = OK;

        // create node scan
        NScan nScan = null;
        if (status == OK) {
            try {
                nScan = nhf.openScan();
            } catch (Exception e) {
                status = FAIL;
                System.err.println("*** Error opening scan\n");
                e.printStackTrace();
            }
        }

        // create btree to index node labels
        ZFile zf = null;
        if (status == OK) {
            try {
                zf = new ZFile(dbName + nameOfZIndex, AttrType.attrString, 82, 1);
                // BT.printAllLeafPages(btf.getHeaderPage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tuple tuple = new Tuple();
            Node node;
            NID nid = new NID();
            boolean done = false;
            while (!done) {
                try {
                    tuple = nScan.getNext(nid);
                    if (tuple == null) {
                        done = true;
                        break;
                    }
                } catch (Exception e) {
                    status = FAIL;
                    e.printStackTrace();
                }
                if (status == OK && !done) {
                    node = new Node(tuple.data, 0);
                    try {
                        KeyClass keyDesc = new DescriptorKey(node.getDesc());
                        KeyClass keyZValue = new StringKey(((DescriptorKey)keyDesc).toZValue());
                        zf.insert(keyZValue, nid); // insertion (yhc)
                        // System.out.println("(" + nid.pageNo.pid + "," + nid.slotNo + ")   " + node.getDesc().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // close node scan
        nScan.closescan();

        return zf;
    }

    /**
     * create a FileScan (iterator) for node
     *
     * @param nodeHeapFileName
     * @return
     */
    public static Iterator createFileScanForNode(String nodeHeapFileName) {

        AttrType[] attrTypes = new AttrType[2]; // fields attribute types
        attrTypes[0] = new AttrType(AttrType.attrString);
        attrTypes[1] = new AttrType(AttrType.attrDesc);

        short[] stringSizes = new short[1]; // size of string field in node
        stringSizes[0] = (short) Node.max_length_of_node_label;

        FldSpec[] projList = new FldSpec[2]; //output - node format
        RelSpec rel = new RelSpec(RelSpec.outer);
        projList[0] = new FldSpec(rel, 1);
        projList[1] = new FldSpec(rel, 2);

        // create file scan on node heap file
        FileScan fscan = null;
        try {
            fscan = new FileScan(nodeHeapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fscan;
    }

    /**
     * create a FileScan (iterator) for edge
     *
     * @param edgeHeapFileName
     * @return
     */
    public static Iterator createFileScanForEdge(String edgeHeapFileName) {

        AttrType[] attrTypes = new AttrType[4];
        attrTypes[0] = new AttrType(AttrType.attrNID);
        attrTypes[1] = new AttrType(AttrType.attrNID);
        attrTypes[2] = new AttrType(AttrType.attrString);
        attrTypes[3] = new AttrType(AttrType.attrInteger);

        short[] stringSizes = new short[1];
        stringSizes[0] = Edge.max_length_of_edge_label;

        FldSpec[] projList = new FldSpec[4]; //output - edge format
        RelSpec rel = new RelSpec(RelSpec.outer);
        projList[0] = new FldSpec(rel, 1);
        projList[1] = new FldSpec(rel, 2);
        projList[2] = new FldSpec(rel, 3);
        projList[3] = new FldSpec(rel, 4);

        // create file scan on edge heap file
        FileScan fscan = null;
        try {
            fscan = new FileScan(edgeHeapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fscan;
    }

    /**
     * create a sort iterator for node, sorted by node labels
     *
     * @param nodeHeapFileName
     * @return
     */
    public static Iterator createSortIteratorForNode(String nodeHeapFileName) {

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

        // create file scan on node heap file
        FileScan fscan = null;
        try {
            fscan = new FileScan(nodeHeapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create sort iterator for node
        Sort sort = null;
        int sortField = 1; // sort on node label
        int sortFieldLength = Node.max_length_of_node_label;
        int SORTPGNUM = 13;
        try {
            sort = new Sort(attrTypes, (short) attrTypes.length, stringSizes, fscan, sortField, order[0], sortFieldLength, SORTPGNUM);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sort;
    }

    /**
     * create a sort iterator for edge, sorted by a field in edge
     *
     * @param edgeHeapFileName
     * @param sortField        sortField=1: sorted by srcLabel; sortField=2: sorted by dstLabel; sortField=3: sorted by edgeLabel; sortField=4: sorted by weight
     * @return
     */
    // Todo: sortField = 1 and sortField = 2, we have NID, but we want to sort by node label, which is the problem we need to solve (pinpage error we add node label field to Edge, cause Edge is too long)
    public static Iterator createSortIteratorForEdge(String edgeHeapFileName, int sortField) {

        AttrType[] attrTypes = new AttrType[4];
        attrTypes[0] = new AttrType(AttrType.attrNID);
        attrTypes[1] = new AttrType(AttrType.attrNID);
        attrTypes[2] = new AttrType(AttrType.attrString);
        attrTypes[3] = new AttrType(AttrType.attrInteger);

        short[] stringSizes = new short[1];
        stringSizes[0] = Edge.max_length_of_edge_label;

        TupleOrder[] order = new TupleOrder[2];
        order[0] = new TupleOrder(TupleOrder.Ascending);
        order[1] = new TupleOrder(TupleOrder.Descending);

        FldSpec[] projList = new FldSpec[4]; //output - edge format
        RelSpec rel = new RelSpec(RelSpec.outer);
        projList[0] = new FldSpec(rel, 1);
        projList[1] = new FldSpec(rel, 2);
        projList[2] = new FldSpec(rel, 3);
        projList[3] = new FldSpec(rel, 4);

        // create file scan on edge heap file
        FileScan fscan = null;
        try {
            fscan = new FileScan(edgeHeapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create sort iterator for node
        Sort sort = null;
        int sortFieldLength;
        if (sortField == 1 || sortField == 2) {
            sortFieldLength = Node.max_length_of_node_label;
        } else {
            sortFieldLength = Edge.max_length_of_edge_label;
        }
        int SORTPGNUM = 13;
        try {
            sort = new Sort(attrTypes, (short) attrTypes.length, stringSizes, fscan, sortField, order[0], sortFieldLength, SORTPGNUM);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sort;
    }

    /**
     * get target NIDs from first NN
     *
     * @param firstNN
     * @param zf z index
     * @param btfNodeLabel btree for node label
     * @return
     */
    public static ArrayList<NID> getNIDsFromFirstNN(String firstNN, ZFile zf, BTreeFile btfNodeLabel) {

        ArrayList<NID> targetNIDs = null;

        if (firstNN.charAt(0) == '(' && firstNN.charAt(firstNN.length() - 1) == ')') { // first NN is node descriptor
            String strDesc = firstNN.substring(1, firstNN.length() - 1);
            String[] dimensions = strDesc.split(",");
            Descriptor targetDescriptor = new Descriptor(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]), Integer.parseInt(dimensions[2]), Integer.parseInt(dimensions[3]), Integer.parseInt(dimensions[4]));

            try {
                targetNIDs = zf.ZFileRangeScan(new DescriptorKey(targetDescriptor), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { // first NN is node label
            String targeLabel = firstNN;
            targetNIDs = new ArrayList<>();

            try {
                BTFileScan btfScan = btfNodeLabel.new_scan(new StringKey(targeLabel), new StringKey(targeLabel));
                while (true) {
                    KeyDataEntry entry = btfScan.get_next();
                    NID nid = null;
                    if (entry != null) {
                        nid = new NID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                        targetNIDs.add(nid);
                    } else {
                        break;
                    }
                }
                btfScan.DestroyBTreeFileScan();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return targetNIDs;
    }

    /**
     * print statistic information
     * @param nhf
     * @param ehf
     */
    public static void printStatInfo(NodeHeapfile nhf, EdgeHeapfile ehf) {
        try {
            System.out.println("Number of nodes: " + nhf.getNodeCnt());
            System.out.println("Number of edges: " + ehf.getEdgeCnt());
            System.out.println("Number of disk pages that were read: " + PCounter.rcounter);
            System.out.println("Number of disk pages that were written: " + PCounter.wcounter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}