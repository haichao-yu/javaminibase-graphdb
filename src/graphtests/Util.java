package graphtests;

import ZIndex.DescriptorKey;
import ZIndex.ZFile;
import btree.*;
import diskmgr.PCounter;
import edgeheap.EScan;
import edgeheap.Edge;
import edgeheap.EdgeHeapfile;
import global.AttrType;
import global.EID;
import global.NID;
import heap.Tuple;
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
     * @param type type=1: index srcLabel; type=2: index dstLabel; type = 3: index edge label
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
