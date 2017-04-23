package graphtests;

import ZIndex.ZFile;
import btree.BTreeFile;
import edgeheap.EdgeHeapfile;
import global.AttrOperator;
import global.AttrType;
import global.NID;
import heap.Tuple;
import iterator.*;
import iterator.Iterator;
import nodeheap.NodeHeapfile;

import java.util.*;

/**
 * Created by nyt on 4/14/17.
 */

public class PathExpressionType3 {

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
    private String[] expr;
    private int type; // type=1 represents PQ3a, type=2 represents PQ3b, type=3 represents PQ3c

    /**
     * Constructor
     *
     * @param dbName
     * @param pathExpression
     */
    public PathExpressionType3(String dbName, String pathExpression, int type) {

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

        // get expression
        expr = pathExpression.split("//");

        // get type
        this.type = type;
    }


    /**
     * task 2.5: given nid, return all edges start from this nid.
     * @param nid
     * @return
     */
    private Iterator operator_3 (NID nid) {

        // create iterator for all node by scanning nodeHeapFile
        FileScan nodeScan = (FileScan) Util.createFileScanForNode(nodeHeapFileName);
        // Initialize joinExpres
        CondExpr[] joinExprs = new CondExpr[2];
        joinExprs[0] = new CondExpr();
        joinExprs[1] = null; //used for ending the while loop
        Iterator edgeIt = null;

        // src node id = target nid
        joinExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
        joinExprs[0].next = null;
        joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
        joinExprs[0].type2 = new AttrType(AttrType.attrNID);
        joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 1);
        joinExprs[0].operand2.nid = nid;

        try {
            //get edge_Iterator with nodeScan(all node) and selection_condition by NodeEdge join.
            edgeIt= new NewIndexNestedLoopJoinsNodeEdge(ehf, btfEdgeSrcLabel, btfEdgeDstLabel, nodeScan, joinExprs, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return edgeIt;
    }

    /**
     * task 2.8, with BFS algorithm
     * boundType 1 -> bound on num_edge; 2 -> bound on total_edge_weight;
     */
    public void Query() {

        ArrayList<Path> result = new ArrayList<>();
        int boundType;
        if(expr[1].startsWith("w:"))
            boundType = 2;
        else boundType = 1;

        // get NID from NN
        String firstNN = expr[0];
        ArrayList<NID> targetNIDs = Util.getNIDsFromFirstNN(firstNN, zf, btfNodeLabel);
        // get bound from expr;
        int bound = Integer.parseInt(expr[1].substring(2));
        int temp_bound = 0;
        if (boundType == 1) {//bound on num_edges
            // for every NID, do Operator(NID)
            for (NID nid : targetNIDs) {
                temp_bound = bound;
                String head = null;
                try {
                    head = nhf.getNode(nid).getLabel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LinkedList<LinkedList<NID>> lists = new LinkedList<>();
                LinkedList<NID> level0 = new LinkedList<>();
                level0.add(nid);
                lists.add(level0);
                int level_idx = 0;
                while (temp_bound > 0) {
                    LinkedList<NID> temp_level = new LinkedList<>();
                    for (int i = 0; i < lists.get(level_idx).size(); i++) {
                        //edge iterator
                        Iterator tails = operator_3(lists.get(level_idx).get(i));
                        Tuple tuple = null;
                        try {
                            while ((tuple = tails.get_next()) != null) {
                                NID nid1 = tuple.getNIDFld(2);
                                String tail = nhf.getNode(nid1).getLabel();
                                result.add(new Path(head, tail));
                                temp_level.add(nid1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    lists.add(temp_level);
                    level_idx++;
                    temp_bound--;
                }
            }
        } else if (boundType == 2){ //bound on total_edge_weight
            //traverse on each nid
            for (NID nid : targetNIDs) {
                temp_bound = bound;
                String head = null;
                try {
                    head = nhf.getNode(nid).getLabel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LinkedList<LinkedList<NidWeight>> lists = new LinkedList<>();
                LinkedList<NidWeight> level0 = new LinkedList<>();
                level0.add(new NidWeight(nid, temp_bound));
                lists.add(level0);
                int level_idx = 0;
                while (true) {
                    LinkedList<NidWeight> temp_level = new LinkedList<>();
                    for (int i = 0; i < lists.get(level_idx).size(); i++) {
                        //get the edge iterator from nid
                        NidWeight nidWeight = lists.get(level_idx).get(i);
                        Iterator it = operator_3(nidWeight.nid);
                        Tuple tuple = null;
                        try {
                            while ((tuple = it.get_next()) != null) {
                                int weight = tuple.getIntFld(4);
                                if (weight > nidWeight.weight) {//out of bound on total_weight
                                    continue;
                                }
                                NID nid1 = tuple.getNIDFld(2);
                                //add path
                                String tail = nhf.getNode(nid1).getLabel();
                                result.add(new Path(head, tail));
                                //add NidWeight with nid1 and new_weight
                                NidWeight nw = new NidWeight(nid1, (nidWeight.weight - weight));
                                temp_level.add(nw);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    level_idx++;
                    lists.add(temp_level);
                    if (temp_level.size() == 0) {
                        break;
                    }
                }
            }
        }


        if (this.type == 1) { // PQ3a: output directly
            for (Path path : result) {
                System.out.println(path.toString());
            }
            System.out.println("Total number of paths: " + result.size());
        } else if (this.type == 2) { // PQ3b: sort the results in the labels of head and tail labels, and output
            Collections.sort(result);
            for (Path path : result) {
                System.out.println(path.toString());
            }
            System.out.println("Total number of paths: " + result.size());
        } else { // PQ3c: output only distinct head/tail node label pairs
            Set<Path> set = new HashSet<>();
            set.addAll(result);
            for (Path path : set) {
                System.out.println(path.toString());
            }
            System.out.println("Total number of paths: " + set.size());
        }
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

    private class NidWeight{
        private NID nid;
        private int weight;
        public NidWeight (NID nid, int weight) {
            this.nid = nid;
            this.weight = weight;
        }
    }
}