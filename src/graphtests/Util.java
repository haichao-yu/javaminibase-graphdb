package graphtests;

import ZIndex.DescriptorKey;
import ZIndex.ZFile;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.KeyClass;
import btree.StringKey;
import diskmgr.PCounter;
import edgeheap.EScan;
import edgeheap.Edge;
import edgeheap.EdgeHeapfile;
import global.*;
import heap.Heapfile;
import heap.Tuple;
import iterator.*;
import nodeheap.NScan;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

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
     * @param outFilter
     * @return
     */
    public static Iterator createFileScanForNode(String nodeHeapFileName, CondExpr[] outFilter) {

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
            fscan = new FileScan(nodeHeapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, outFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fscan;
    }

    /**
     * create a FileScan (iterator) for edge
     *
     * @param edgeHeapFileName
     * @param outFilter
     * @return
     */
    public static Iterator createFileScanForEdge(String edgeHeapFileName, CondExpr[] outFilter) {

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
            fscan = new FileScan(edgeHeapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, outFilter);
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
     * @param oriFirstNN
     * @param zf z index
     * @param btfNodeLabel btree for node label
     * @return
     */
    /*
    public static ArrayList<NID> getNIDsFromFirstNN(String oriFirstNN, ZFile zf, BTreeFile btfNodeLabel) {

        ArrayList<NID> targetNIDs = null;
        String firstNN = oriFirstNN.substring(2);
        if(oriFirstNN.startsWith("d:")){
            if (firstNN.charAt(0) == '(' && firstNN.charAt(firstNN.length() - 1) == ')') { // first NN is node descriptor
                String strDesc = firstNN.substring(1, firstNN.length() - 1);
                String[] dimensions = strDesc.split(",");
                Descriptor targetDescriptor = new Descriptor(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]), Integer.parseInt(dimensions[2]), Integer.parseInt(dimensions[3]), Integer.parseInt(dimensions[4]));

                try {
                    targetNIDs = zf.ZFileRangeScan(new DescriptorKey(targetDescriptor), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else { // first NN is node label
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
    */

    /**
     * get first node iterator given first NN (utilizing outFilter of FileScan)
     *
     * @param oriFirstNN
     * @param NodeHeapfileName
     * @return
     */
    public static Iterator getFirstNodesFromFirstNN(String oriFirstNN, String NodeHeapfileName) {

        CondExpr[] outFilter = new CondExpr[2];
        outFilter[0] = new CondExpr();
        outFilter[1] = null;

        String firstNN = oriFirstNN.substring(2);
        if (oriFirstNN.startsWith("d:")) { // first NN is node descriptor
            String strDesc = firstNN.substring(1, firstNN.length() - 1);
            String[] dimensions = strDesc.split(",");
            Descriptor targetDescriptor = new Descriptor(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]), Integer.parseInt(dimensions[2]), Integer.parseInt(dimensions[3]), Integer.parseInt(dimensions[4]));

            outFilter[0].op = new AttrOperator(AttrOperator.aopEQ);
            outFilter[0].next = null;
            outFilter[0].type1 = new AttrType(AttrType.attrSymbol);
            outFilter[0].type2 = new AttrType(AttrType.attrDesc);
            outFilter[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 2);
            outFilter[0].operand2.desc = new Descriptor(targetDescriptor);
        } else { // first NN is node label
            String targeLabel = firstNN;
            outFilter[0].op = new AttrOperator(AttrOperator.aopEQ);
            outFilter[0].next = null;
            outFilter[0].type1 = new AttrType(AttrType.attrSymbol);
            outFilter[0].type2 = new AttrType(AttrType.attrString);
            outFilter[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 1);
            outFilter[0].operand2.string = new String(firstNN);
        }

        Iterator iterator = Util.createFileScanForNode(NodeHeapfileName, outFilter);
        return iterator;
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

    /**
     * first time to save the triangles into a heap file
     * @param data
     * @param file_name
     */
    public static Heapfile createHeapFileFromTriResult(ArrayList<Triangle> data, String file_name){
        Heapfile result = null;
        boolean status = OK;
        try {
            result = new NodeHeapfile(file_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (status == OK && SystemDefs.JavabaseBM.getNumUnpinnedBuffers()
                != SystemDefs.JavabaseBM.getNumBuffers()) {

            System.err.println("*** Insertion left a page pinned\n");
            status = FAIL;
        }
        Tuple tuple = null;
        short num_of_filds = 3;
        AttrType[] attrTypes = new AttrType[3];
        attrTypes[0] = new AttrType(AttrType.attrString);
        attrTypes[1] = new AttrType(AttrType.attrString);
        attrTypes[2] = new AttrType(AttrType.attrString);
        short[] strsize = {Node.max_length_of_node_label, Node.max_length_of_node_label, Node.max_length_of_node_label};

        tuple = new Tuple();
        try {
            tuple.setHdr(num_of_filds,attrTypes,strsize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // insert data to heapfile
        if (status == OK) {
            for (int i = 0; i < data.size(); i++) {
                try {
                    tuple.setStrFld(1, data.get(i).getTrianNode1());
                    tuple.setStrFld(2, data.get(i).getTrianNode2());
                    tuple.setStrFld(3, data.get(i).getTrianNode3());
                    result.insertRecord(tuple.getTupleByteArray());
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
        return result;
    }

    /**
     * create file scan for partially sort the triangle result
     * @param heapFileName
     * @param label Strings contain the already sorted field
     * @param sortField the field to be sorted.
     */
    public static Iterator createFileScanForTriResult(String heapFileName, String[] label, int sortField) {

        AttrType[] attrTypes = new AttrType[3]; // fields attribute types
        attrTypes[0] = new AttrType(AttrType.attrString);
        attrTypes[1] = new AttrType(AttrType.attrString);
        attrTypes[2] = new AttrType(AttrType.attrString);

        short[] stringSizes = new short[3]; // size of string field in node
        stringSizes[0] = (short) Node.max_length_of_node_label;
        stringSizes[1] = (short) Node.max_length_of_node_label;
        stringSizes[2] = (short) Node.max_length_of_node_label;

        FldSpec[] projList = new FldSpec[3]; //output - (l1, l2, l3)
        RelSpec rel = new RelSpec(RelSpec.outer);
        projList[0] = new FldSpec(rel, 1);
        projList[1] = new FldSpec(rel, 2);
        projList[2] = new FldSpec(rel, 3);

        //add selecting condition
        CondExpr[] condExprs = null;
        if (label != null) {
            if(sortField == 2) {
                condExprs = new CondExpr[2];
                condExprs[0] = new CondExpr();
                condExprs[1] = null;
                condExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
                condExprs[0].next = null;
                condExprs[0].type1 = new AttrType(AttrType.attrSymbol);
                condExprs[0].type2 = new AttrType(AttrType.attrString);
                condExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 1);
                condExprs[0].operand2.string = label[0];
            }
            // the first two labels are already sorted, match them to sort the third.
            if(sortField == 3) {
                condExprs = new CondExpr[3];
                condExprs[0] = new CondExpr();
                condExprs[1] = new CondExpr();
                condExprs[2] = null;
                condExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
                condExprs[0].next = null;
                condExprs[0].type1 = new AttrType(AttrType.attrSymbol);
                condExprs[0].type2 = new AttrType(AttrType.attrString);
                condExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 1);
                condExprs[0].operand2.string = label[0];
                condExprs[1].op = new AttrOperator(AttrOperator.aopEQ);
                condExprs[1].next = null;
                condExprs[1].type1 = new AttrType(AttrType.attrSymbol);
                condExprs[1].type2 = new AttrType(AttrType.attrString);
                condExprs[1].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 2);
                condExprs[1].operand2.string = label[1];
            }
        }
        // create file scan on node heap file
        FileScan fscan = null;
        try {
            fscan = new FileScan(heapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, condExprs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fscan;
    }

    /**
     * create heapfile from trisort.
     * @param sort
     * @param file_name
     * @param sortLabels - the labels of the field to be sorted
     * @param sortField - the offset of the field to be sorted
     */
    public static Heapfile createHeapFileFromTriSort(Sort sort, String file_name, ArrayList<String> sortLabels, int sortField){
        Heapfile result = null;
        boolean status = OK;
        HashSet<String> set = new HashSet<>();
        String sortLabel = null;
        try {
            result = new NodeHeapfile(file_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Tuple tuple = null;
        short num_of_filds = 3;
        AttrType[] attrTypes = new AttrType[3];
        attrTypes[0] = new AttrType(AttrType.attrString);
        attrTypes[1] = new AttrType(AttrType.attrString);
        attrTypes[2] = new AttrType(AttrType.attrString);
        short[] strsize = {Node.max_length_of_node_label, Node.max_length_of_node_label, Node.max_length_of_node_label};
        // insert data to heapfile
        if (status == OK) {
            try {
                while ((tuple = sort.get_next()) != null) {
                    sortLabel = tuple.getStrFld(sortField);
                    if (!set.contains(sortLabel)) {
                        set.add(sortLabel);
                        sortLabels.add(sortLabel);
                    }
                    result.insertRecord(tuple.getTupleByteArray());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /******************************start of new methods Create by Yutang Nie*********************************/
    /**
     * Store query result into file, with given file_name
     * @param data query path result
     * @param file_name
     * @return
     */
    public static Heapfile createHeapFileFromPathResult(ArrayList<Path> data, String file_name){
        Heapfile result = null;
        try {
            result = new NodeHeapfile(file_name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Tuple tuple = null;
        short num_of_filds = 2;
        AttrType[] attrTypes = new AttrType[2];
        attrTypes[0] = new AttrType(AttrType.attrString);
        attrTypes[1] = new AttrType(AttrType.attrString);
        short[] strsize = {Node.max_length_of_node_label, Node.max_length_of_node_label};
        // insert data to heapfile
        for (int i = 0; i < data.size(); i++) {
            tuple = new Tuple();
            try {
                tuple.setHdr(num_of_filds,attrTypes,strsize);
                tuple.setStrFld(1, data.get(i).getHead());
                tuple.setStrFld(2, data.get(i).getTail());
                result.insertRecord(tuple.getTupleByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return result;
    }

    /**
     * create iterator from given heapFileName
     * @param heapFileName
     * @param label label is used for selection, if label == null, no selection
     * @return
     */
    public static Iterator createFileScanForPathResult(String heapFileName, String label) {

        AttrType[] attrTypes = new AttrType[2]; // fields attribute types
        attrTypes[0] = new AttrType(AttrType.attrString);
        attrTypes[1] = new AttrType(AttrType.attrString);

        short[] stringSizes = new short[2]; // size of string field in node
        stringSizes[0] = (short) Node.max_length_of_node_label;
        stringSizes[1] = (short) Node.max_length_of_node_label;

        FldSpec[] projList = new FldSpec[2]; //output - (head, tail)
        RelSpec rel = new RelSpec(RelSpec.outer);
        projList[0] = new FldSpec(rel, 1);
        projList[1] = new FldSpec(rel, 2);

        //add selecting condition
        CondExpr[] condExprs = null;
        if (label != null) {
            condExprs = new CondExpr[2];
            condExprs[0] = new CondExpr();
            condExprs[1] = null;
            // src node id = target nid
            condExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
            condExprs[0].next = null;
            condExprs[0].type1 = new AttrType(AttrType.attrSymbol);
            condExprs[0].type2 = new AttrType(AttrType.attrString);
            condExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 1);
            condExprs[0].operand2.string = label;
        }
        // create file scan; if label != null, use selection -> head = label || tail = label
        FileScan fscan = null;
        try {
            fscan = new FileScan(heapFileName, attrTypes, stringSizes, (short) attrTypes.length, projList.length, projList, condExprs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fscan;
    }

    /**
     * store first round sorted iterator to file with given file name
     * @param sort already sorted iterator from the first round, sort only on head
     * @param file_name
     * @param heads used to store each distinct head
     * @return
     */
    public static Heapfile createHeapFileFromSort(Sort sort, String file_name, ArrayList<String> heads){
        Heapfile result = null;
        HashSet<String> set = new HashSet<>();
        String head = null;
        try {
            result = new NodeHeapfile(file_name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Tuple tuple = null;

        // insert data to heapfile
        try {
            while ((tuple = sort.get_next()) != null) {
                head = tuple.getStrFld(1);
                if (!set.contains(head)) {
                    set.add(head);
                    heads.add(head);
                }
                result.insertRecord(tuple.getTupleByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    /**
     * sort the result query data, and return the number of path
     * @param result
     * @param type type = 2 -> print in order; type = 3 -> print only distinct one
     * @param fileName given fileName to store query result
     * @return count of path
     */
    public static int sortAndPrint(ArrayList<Path> result, int type, String fileName){
        ArrayList<String> heads = new ArrayList<>();
        int count = 0;
        //store path result into a file
        String pathHeapFileName = fileName;
        Heapfile heapfile = Util.createHeapFileFromPathResult(result, pathHeapFileName);
        //create file scan
        Iterator pathScan = Util.createFileScanForPathResult(pathHeapFileName, null);
        AttrType[] attrType = new AttrType[2];
        attrType[0] = new AttrType(AttrType.attrString);
        attrType[1] = new AttrType(AttrType.attrString);
        short[] attrSize = new short[2];
        attrSize[0] = Node.max_length_of_node_label;
        attrSize[1] = Node.max_length_of_node_label;
        TupleOrder[] order = new TupleOrder[2];
        order[0] = new TupleOrder(TupleOrder.Ascending);
        order[1] = new TupleOrder(TupleOrder.Descending);

        Sort sort = null;
        try {
            sort = new Sort(attrType, (short) 2, attrSize, pathScan, 1, order[0], Node.max_length_of_node_label, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sortedHeadHeapFileName = "PQ_result_head_sorted";
        Heapfile heapfile2 = Util.createHeapFileFromSort(sort, sortedHeadHeapFileName, heads);
        try {
            sort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Iterator pathScan2 = null;
        String head = null;
        Sort sort2 = null;

        //for eliminate duplicate
        AttrType[] edtype = {new AttrType(AttrType.attrString), new AttrType(AttrType.attrString)};
        short[] edsizes = new short[2];
        edsizes[0] = Node.max_length_of_node_label;
        edsizes[1] = Node.max_length_of_node_label;
        Iterator iterator = null;
        for (int i = 0; i < heads.size(); i++) {
            head = heads.get(i);
            pathScan2 = Util.createFileScanForPathResult(sortedHeadHeapFileName, head);
            try {
                sort2 = new Sort(attrType, (short) 2, attrSize, pathScan2, 2, order[0], Node.max_length_of_node_label, 50);
                if (type == 2) {//only sort
                    iterator = sort2;
                } else if (type == 3){// sort then eliminate duplicate
                    iterator = new DuplElim(edtype, (short) 2, edsizes, sort2, 100, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tuple tuplePath = null;
            try {
                while ((tuplePath = iterator.get_next()) != null) {
                    count++;
                    System.out.println(tuplePath.getStrFld(1) + " -> " + tuplePath.getStrFld(2));
                }
                pathScan2.close();
                sort2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            heapfile.deleteFile();
            heapfile2.deleteFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
    /****************************** End of new methods Create by Yutang Nie*********************************/
}