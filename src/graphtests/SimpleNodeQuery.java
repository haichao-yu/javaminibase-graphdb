package graphtests;

import ZIndex.DescriptorKey;
import ZIndex.ZFile;
import btree.*;
import edgeheap.Edge;
import edgeheap.EdgeHeapfile;
import global.*;
import heap.Tuple;
import nodeheap.NScan;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.util.*;

/**
 * Created by yhc on 3/15/17.
 */
public class SimpleNodeQuery implements GlobalConst{

    private final static boolean OK = true;
    private final static boolean FAIL = false;

    public static void runSimpleNodeQuery(String[] arguments) {

        // arguments[0] = "nodequery"
        // arguments[1] = "GRAPHDBNAME"
        // arguments[2] = "NUMBUF"
        // arguments[3] = "QTYPE"
        // arguments[4] = "INDEX"

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


        /*
        try {
            System.out.print(nhf.getNodeCnt());
            Node node = nhf.getNode(new NID(new PageId(3), 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        switch (qtype) {
            case 0:
                queryType0(nhf);
                break;
            case 1:
                queryType1(dbName, nhf, index);
                break;
            case 2:
                queryType2(nhf);
                break;
            case 3:
                queryType3(dbName, nhf);
                break;
            case 4:
                queryType4(dbName, nhf, ehf);
                break;
            case 5:
                queryType5(dbName, nhf, ehf);
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
     * print the node data in the order it occurs in node heap
     * @param nhf
     */
    private static void queryType0(NodeHeapfile nhf) {

        System.out.println("Print the node data in the order it occurs in node heap:");

        // create node scan
        NScan nScan = null;
        try {
            nScan = nhf.openScan();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // scan and print node data in the order it occurs in the node heap
        Tuple tuple;
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
                if (!done) {
                    node = new Node(tuple.data, 0);
                    node.print();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // close scan
        nScan.closescan();

        System.out.println("Query completed.\n");
    }

    /**
     * print the node data in increasing alphanumerical order of labels
     * @param dbName
     * @param nhf
     * @param index
     */
    private static void queryType1(String dbName, NodeHeapfile nhf, int index) {

        System.out.println("Print the node data in increasing alphanumerical order of labels:");

        if (index == 1) { // processed using an index
            BTreeFile btf = Util.createBtreeFromStringKeyForNode(dbName, "btree_node_label", nhf); // bree没有destroy掉，重新new btree会有原来的数据，导致出错 (problem solved)
            try {
                BTFileScan btfScan = btf.new_scan(null, null);
                while (true) {
                    KeyDataEntry entry = btfScan.get_next();
                    NID nid = null;
                    if (entry != null) {
                        nid = new NID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                        Node node = nhf.getNode(nid);
                        node.print();
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
                btf.destroyFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else { // processed using only the relevant heap files
            List<Node> alist = new ArrayList<>();
            // create node scan
            NScan nScan = null;
            try {
                nScan = nhf.openScan();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // scan and add node to alist
            Tuple tuple;
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
                    if (!done) {
                        node = new Node(tuple.data, 0);
                        alist.add(node);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // close scan
            nScan.closescan();
            // sort
            Collections.sort(alist);
            // print nodes
            for (Node anode : alist) {
                try {
                    anode.print();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Query completed.\n");
    }

    /**
     * take a target descriptor and a distance, and print the node data in increasing order of distance from a given 5D target descriptor
     * @param nhf
     */
    private static void queryType2(NodeHeapfile nhf) {

        // get target descriptor
        Scanner in = new Scanner(System.in);
        System.out.println("Please input a target descriptor:");
        String strDesc = in.nextLine();
        String[] strs = strDesc.split(" ");
        targetDesc = new Descriptor(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3]), Integer.parseInt(strs[4]));


        System.out.println("Print the node data in increasing order of distance from a given 5D target descriptor: ");

        List<Node> nodes = new ArrayList<>();

        // create node scan
        NScan nScan = null;
        try {
            nScan = nhf.openScan();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // scan and add nodes to list
        Tuple tuple;
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
                if (!done) {
                    node = new Node(tuple.data, 0);
                    nodes.add(node);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // close scan
        nScan.closescan();

        // sort and print
        try {
            Collections.sort(nodes, DescriptorComparator);
            for (Node anode : nodes) {
                anode.print();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Query completed.\n");
    }

    /**
     * take a target descriptor and a distance, and return the labels of nodes within the given distance from the target descriptor
     * @param dbName
     * @param nhf
     */
    private static void queryType3(String dbName, NodeHeapfile nhf) {

        // get target descriptor and distance
        Scanner in = new Scanner(System.in);
        System.out.println("Please input a target descriptor:");
        String strDesc = in.nextLine();
        String[] strs = strDesc.split(" ");
        Descriptor target = new Descriptor(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3]), Integer.parseInt(strs[4]));
        System.out.println("Please input a distance:");
        double distance = in.nextDouble();

        System.out.println("Print labels of nodes within the given distance(" + distance + ") from the target descriptor" + target.toString() + ": ");

        ArrayList<NID> result = null;

        // build Z tree
        ZFile zf = Util.createZIndexFromZValueForNode(dbName, "zindex", nhf);
        try {
            // BT.printAllLeafPages(zf.getHeaderPage());
            result = zf.ZFileRangeScan(new DescriptorKey(target), distance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            zf.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (NID nid : result) {
            try {
                System.out.println(nhf.getNode(nid).getLabel());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Query completed.\n");
    }

    /**
     * take a albel, and return all relevant information (including outgoing and incoming edges) about the node with the matching label (if any)
     * @param dbName
     * @param nhf
     * @param ehf
     */
    private static void queryType4(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf) {

        // get target descriptor
        Scanner in = new Scanner(System.in);
        System.out.println("Please input a node label:");
        String label = in.nextLine();

        System.out.println("Print relevant information about the node with the label " + label + ":");

        getRelevantInfoFromNodeLabel(dbName, nhf, ehf, label);

        System.out.println("Query completed.\n");
    }

    /**
     * take a target descriptor and a distance, and return all relevant information (including outgoing and incoming edges) about the nodes within the given distance from the target descriptor
     * @param dbName
     * @param nhf
     * @param ehf
     */
    private static void queryType5(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf) {

        // get target descriptor and distance
        Scanner in = new Scanner(System.in);
        System.out.println("Please input a target descriptor:");
        String strDesc = in.nextLine();
        String[] strs = strDesc.split(" ");
        Descriptor target = new Descriptor(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3]), Integer.parseInt(strs[4]));
        System.out.println("Please input a distance:");
        double distance = in.nextDouble();

        System.out.println("Print relevant information of nodes within the given distance(" + distance + ") from the target descriptor" + target.toString() + ": ");

        ArrayList<NID> result = null;

        // build Z tree
        ZFile zf = Util.createZIndexFromZValueForNode(dbName, "zindex", nhf);
        try {
            // BT.printAllLeafPages(zf.getHeaderPage());
            result = zf.ZFileRangeScan(new DescriptorKey(target), distance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            zf.destroyFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (NID nid : result) {
            try {
                String label = nhf.getNode(nid).getLabel();
                getRelevantInfoFromNodeLabel(dbName, nhf, ehf, label);
                System.out.println();
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
     * @param label
     */
    private static void getRelevantInfoFromNodeLabel(String dbName, NodeHeapfile nhf, EdgeHeapfile ehf, String label) {

        // create btree to index node labels
        BTreeFile btf = Util.createBtreeFromStringKeyForNode(dbName, "btree_node_label", nhf);
        // create btree to index srcLabel of edges
        BTreeFile btfSrcLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_srcLabel", nhf, ehf, 1);
        // create btree to index dstLabel of edges
        BTreeFile btfDstLabel = Util.createBtreeFromStringKeyForEdge(dbName, "btree_edge_dstLabel", nhf, ehf, 2);

        // print relevant information
        BTFileScan btfScan = null;
        StringKey key = new StringKey(label);
        KeyDataEntry entry = null;
        NID nid = null;
        EID eid = null;
        Node node = null;
        List<EID> outgoing = new ArrayList<>();
        List<EID> incoming = new ArrayList<>();
        boolean status = OK;
        try {
            // find node
            btfScan = btf.new_scan(key, key);
            entry = btfScan.get_next();
            if (entry == null) { // node does not exist
                System.out.println("The node you input is not exist!");
                status = FAIL;
            }
            btfScan.DestroyBTreeFileScan(); // first destroy btfScan (unpin)
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (status == OK) {
            try {
                nid = new NID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                node = nhf.getNode(nid);
                // find outgoing edges
                btfScan = btfSrcLabel.new_scan(key, key);
                while (true) {
                    entry = btfScan.get_next();
                    if (entry != null) {
                        eid = new EID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                        outgoing.add(eid);
                    } else {
                        break;
                    }
                }
                btfScan.DestroyBTreeFileScan();
                // find incoming edges
                btfScan = btfDstLabel.new_scan(key, key);
                while (true) {
                    entry = btfScan.get_next();
                    if (entry != null) {
                        eid = new EID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                        incoming.add(eid);
                    } else {
                        break;
                    }
                }
                btfScan.DestroyBTreeFileScan();
            } catch (Exception e) {
                e.printStackTrace();
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

        if (status == OK) {
            System.out.println("----------------------------------------");
            System.out.println("NID: " + nid.toString());
            System.out.println("Node Label: " + label);
            try {
                System.out.println("Node Descriptor: " + node.getDesc().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.print("Outgoing Edges: ");
            for (int i = 0; i < outgoing.size(); i++) {
                try {
                    Edge edge = ehf.getEdge(outgoing.get(i));
                    System.out.print(edge.getLabel() + "   ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
            System.out.print("Incoming Edges: ");
            for (int i = 0; i < incoming.size(); i++) {
                try {
                    Edge edge = ehf.getEdge(incoming.get(i));
                    System.out.print(edge.getLabel() + "   ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
            System.out.println("----------------------------------------");
        }
    }



    // used in query 2
    private static Descriptor targetDesc = new Descriptor();
    private static Comparator<Node> DescriptorComparator
            = new Comparator<Node>() {
        @Override
        public int compare(Node n1, Node n2) {
            Descriptor d1 = null;
            Descriptor d2 = null;
            try {
                d1 = new Descriptor(((Node) n1).getDesc());
                d2 = new Descriptor(((Node) n2).getDesc());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (int)Math.round(d1.distance(targetDesc) - d2.distance(targetDesc));
        }
    };
}