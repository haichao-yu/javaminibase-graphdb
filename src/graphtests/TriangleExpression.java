package graphtests;

import diskmgr.PCounter;
import edgeheap.Edge;
import edgeheap.EdgeHeapfile;
import global.AttrOperator;
import global.AttrType;
import global.NID;
import global.TupleOrder;
import heap.Heapfile;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import iterator.*;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by changchu on 4/15/17.
 * Given triangle EN1;EN2;EN3
 * with each EN = w:EN' | l:EN', the following EN' is the actual EN
 */
public class TriangleExpression {
    private GraphDBManager gdb;
    private String dbName;
    private String nodeHeapFileName;
    private String edgeHeapFileName;

    private NodeHeapfile nhf;
    private EdgeHeapfile ehf;

    private String[] EN;
    private int[] ENType; // 1: label, 2: weight
    private int type; //type = 1: returns the labels of the corresponding three nodes,
    // type = 2: sorts the results in the labels of the corresponding nodes,
    // type = 3: only distinct node label triples

    public TriangleExpression(String dbName, String pathExpression, int type) {
        PCounter.initialize();
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


        // get ENs
        String[] getEN;
        EN = new String[3];
        getEN = pathExpression.split(";");
        ENType = new int[3];
        for (int i = 0; i < 3; i++) {
            if (getEN[i].startsWith("l:")) {
                ENType[i] = 1;
            }
            else ENType[i] = 2;
            EN[i] = getEN[i].substring(2);
        }
        // get type
        this.type = type;
    }

    /**
     * Task 2.9
     */
    public void Query() {

        Iterator nlj = getTriangleNIDs(this);
        Tuple tuple = new Tuple();
        AttrType[] types = new AttrType[3];
        types[0] = new AttrType(AttrType.attrNID);
        types[1] = new AttrType(AttrType.attrNID);
        types[2] = new AttrType(AttrType.attrNID);
        short[] sizes = null;
        ArrayList<Triangle> triResult = new ArrayList<>();
        ArrayList<String> firstLabels = new ArrayList<>();
        try {
            tuple.setHdr((short) 3, types, sizes);
            while ((tuple = nlj.get_next()) != null) {
                NID NID1 = tuple.getNIDFld(1);
                NID NID2 = tuple.getNIDFld(2);
                NID NID3 = tuple.getNIDFld(3);
                String node1 = nhf.getNode(NID1).getLabel();
                String node2 = nhf.getNode(NID2).getLabel();
                String node3 = nhf.getNode(NID3).getLabel();
                triResult.add(new Triangle(node1,node2,node3));
            }
            nlj.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.type == 1) { // 2.9 PQ1a: output directly
            for (Triangle triangle : triResult) {
                System.out.println(triangle.toString());
            }
            System.out.println("Total number of triangles: " + triResult.size());
        }


        else {
            //store triangle result into a file
            String triResultHeapFileName = "TQ_result";
            Heapfile heapfile = Util.createHeapFileFromTriResult(triResult, triResultHeapFileName);
            //create file scan
            Iterator triScan = Util.createFileScanForTriResult(triResultHeapFileName, null, 1);
            AttrType[] attrType = new AttrType[3];
            attrType[0] = new AttrType(AttrType.attrString);
            attrType[1] = new AttrType(AttrType.attrString);
            attrType[2] = new AttrType(AttrType.attrString);
            short[] attrSize = new short[3];
            attrSize[0] = Node.max_length_of_node_label;
            attrSize[1] = Node.max_length_of_node_label;
            attrSize[2] = Node.max_length_of_node_label;
            TupleOrder order = new TupleOrder(TupleOrder.Ascending);

            int count = 0;

            Sort sort = null;
            try {
                sort = new Sort(attrType, (short) 3, attrSize, triScan, 1, order, Node.max_length_of_node_label, 50);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String sortedFirstLabelHeapFileName = "TQ_result_label1_sorted";
            Heapfile heapfile2 = Util.createHeapFileFromTriSort(sort, sortedFirstLabelHeapFileName, firstLabels, 1);
            try {
                sort.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Iterator triScan2 = null;
            String[] labels = new String[2];
            Sort sort2 = null;
            for (int i = 0; i < firstLabels.size(); i++) {
                labels[0] = firstLabels.get(i);
                labels[1] = null;
                triScan2 = Util.createFileScanForTriResult(sortedFirstLabelHeapFileName, labels, 2);
                try {
                    sort2 = new Sort(attrType, (short) 3, attrSize, triScan2, 2, order, Node.max_length_of_node_label, 50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ArrayList<String> secondLabels = new ArrayList<>();
                String sortedSecondLabelHeapFileName = "TQ_result_label2_sorted";
                Heapfile heapfile3 = Util.createHeapFileFromTriSort(sort2, sortedSecondLabelHeapFileName, secondLabels, 2);
                for (int j = 0; j < secondLabels.size(); j++) {
                    labels[1] = secondLabels.get(j);
                    Iterator triScan3 = null;
                    Sort sort3 = null;
                    triScan3 = Util.createFileScanForTriResult(sortedSecondLabelHeapFileName, labels, 3);
                    try {
                        sort3 = new Sort(attrType, (short) 3, attrSize, triScan3, 3, order, Node.max_length_of_node_label, 50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Tuple tupleTriangle = null;
                    try {
                        if(type == 2) {
                            while ((tupleTriangle = sort3.get_next()) != null) {
                                count++;
                                System.out.println(tupleTriangle.getStrFld(1) + " -> " +
                                        tupleTriangle.getStrFld(2) + " -> " + tupleTriangle.getStrFld(3));
                            }
                        }
                        else {
                            DuplElim ed = null;
                            try {
                                ed = new DuplElim(attrType, (short) 3, attrSize, sort3, 10, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            while ((tupleTriangle = ed.get_next()) != null) {
                                count++;
                                System.out.println(tupleTriangle.getStrFld(1) + " -> " +
                                        tupleTriangle.getStrFld(2) + " -> " + tupleTriangle.getStrFld(3));
                            }
                            ed.close();
                        }
                        sort3.close();
                        triScan3.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    sort2.close();
                    triScan2.close();
                    heapfile3.deleteFile();
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
            System.out.println("Total number of triangles: " + count);
        }


//        else if (this.type == 2) { // 2.9 PQ1b: sort the results in the corresponding nodes of triangles
//            Collections.sort(triResult);
//            for (Triangle triangle : triResult) {
//                System.out.println(triangle.toString());
//            }
//            System.out.println("Total number of triangles: " + triResult.size());
//        } else { // 2.9 PQ1c: output only distinct label of nodes
//            Set<Triangle> set = new HashSet<>();
//            set.addAll(triResult);
//            for (Triangle triangle : set) {
//                System.out.println(triangle.toString());
//            }
//            System.out.println("Total number of triangles: " + set.size());
//        }

        System.out.print("Plan used: \n    ");
        if(this.type == 2)
            System.out.print("SORT ");
        if(this.type == 3)
            System.out.print("DISTINCT ");
        System.out.print(
                "Pi(Edge1.source_NID, Edge1.dest_NID, Edge2.dest_NID)\n" +
                "    Sigma(");
        for(int k = 1; k < 4; k++) {
            if (ENType[k-1] == 1)
                System.out.print("Edge"+ k +".label = \"" + EN[k-1] + "\", ");
            if (ENType[k-1] == 2)
                System.out.print("Edge"+ k +".weight = \"" + EN[k-1] + "\", ");
        }
        System.out.print("Edge1.source_NID = Edge.dest_NID, Edge2.dest_NID = Edge.source_NID \n" +
                "       ");
        System.out.print("((Pi(Edge1.source_NID, Edge1.dest_NID, Edge2.dest_NID, " +
                "Edge1.label, Edge2.label, Edge1.weight, Edge2.weight) \n" +
                "       (Edge1 |><| Edge2)) \n" +
                "           |><| Edge)\n" +
                "   --- Edge1, Edge2 and Edge are all file scan on edge heap file, \n" +
                "   --- first use sort-merge join on Edge1 and Edge2, the second join is nested-loop join.\n");

        // print statistic information
        Util.printStatInfo(nhf, ehf);
        System.out.println();
    }


    public void close() {
        gdb.close();
    }

    public Iterator getTriangleNIDs(TriangleExpression TE) {
        NestedLoopsJoins nlj;
        // AttrTypes of outer and inner
        // out: smj (NID1, NID2, NID3, label1, label2, weight1, weight2)
        // inner:edge
        AttrType[] smjTypes = new AttrType[7];
        smjTypes[0] = new AttrType(AttrType.attrNID);
        smjTypes[1] = new AttrType(AttrType.attrNID);
        smjTypes[2] = new AttrType(AttrType.attrNID);
        smjTypes[3] = new AttrType(AttrType.attrString);
        smjTypes[4] = new AttrType(AttrType.attrString);
        smjTypes[5] = new AttrType(AttrType.attrInteger);
        smjTypes[6] = new AttrType(AttrType.attrInteger);
        AttrType[] edgeTypes = new AttrType[4];
        edgeTypes[0] = new AttrType(AttrType.attrNID);
        edgeTypes[1] = new AttrType(AttrType.attrNID);
        edgeTypes[2] = new AttrType(AttrType.attrString);
        edgeTypes[3] = new AttrType(AttrType.attrInteger);

        // String size of outer, inner and output
        short[] smjStrSize = new short[2];
        smjStrSize[0] = Edge.max_length_of_edge_label;
        smjStrSize[1] = Edge.max_length_of_edge_label;
        short[] edgeStrSize = new short[1];
        edgeStrSize[0] = Edge.max_length_of_edge_label;
        short[] outputStrSize = null;

        // AttrTypes of the output
        AttrType[] outputTypes = new AttrType[3];
        outputTypes[0] = new AttrType(AttrType.attrNID);
        outputTypes[1] = new AttrType(AttrType.attrNID);
        outputTypes[2] = new AttrType(AttrType.attrNID);

        // what fields to be projected to the output
        FldSpec[] proj_list = new FldSpec[3];
        RelSpec outer = new RelSpec(RelSpec.outer);
        proj_list[0] = new FldSpec(outer, 1);
        proj_list[1] = new FldSpec(outer, 2);
        proj_list[2] = new FldSpec(outer, 3);

        // Setup the output tuple
        Tuple outputTuple = new Tuple();

        short[] ts_size = null;
        try {
            ts_size = TupleUtils.setup_op_tuple(outputTuple, outputTypes,
                    smjTypes, 7, edgeTypes, 4,
                    smjStrSize, edgeStrSize,
                    proj_list, 3);
        } catch (Exception e) {
            try {
                throw new TupleUtilsException(e, "Exception is caught by SortMerge.java");
            } catch (TupleUtilsException e1) {
                e1.printStackTrace();
            }
        }

        CondExpr[] joinExprs = new CondExpr[6];
        condExprSetUp(joinExprs);

        try {
            Iterator smj = new NewSortMergeJoins(edgeHeapFileName);
            nlj = new NestedLoopsJoins(smjTypes, 7, smjStrSize, edgeTypes, 4, edgeStrSize,
                    40, smj, edgeHeapFileName, joinExprs, null, proj_list, 3);
            return nlj;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void condExprSetUp(CondExpr[] joinExprs) {
        joinExprs[0] = new CondExpr();
        joinExprs[1] = new CondExpr();
        joinExprs[2] = new CondExpr();
        joinExprs[3] = new CondExpr();
        joinExprs[4] = new CondExpr();
        joinExprs[5] = null;

        //label
        if (ENType[0] == 1) {
            joinExprs[0].op = new AttrOperator(AttrOperator.aopEQ);
            joinExprs[0].next = null;
            joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
            joinExprs[0].type2 = new AttrType(AttrType.attrString);
            joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 4);
            joinExprs[0].operand2.string = EN[0];
        }
        // weight
        else {
            joinExprs[0].op = new AttrOperator(AttrOperator.aopLE);
            joinExprs[0].next = null;
            joinExprs[0].type1 = new AttrType(AttrType.attrSymbol);
            joinExprs[0].type2 = new AttrType(AttrType.attrInteger);
            joinExprs[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 6);
            joinExprs[0].operand2.integer = Integer.parseInt(EN[0]);
        }

        //label
        if (ENType[1] == 1) {
            joinExprs[1].op = new AttrOperator(AttrOperator.aopEQ);
            joinExprs[1].next = null;
            joinExprs[1].type1 = new AttrType(AttrType.attrSymbol);
            joinExprs[1].type2 = new AttrType(AttrType.attrString);
            joinExprs[1].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 5);
            joinExprs[1].operand2.string = EN[1];
        }
        // weight
        else {
            joinExprs[1].op = new AttrOperator(AttrOperator.aopLE);
            joinExprs[1].next = null;
            joinExprs[1].type1 = new AttrType(AttrType.attrSymbol);
            joinExprs[1].type2 = new AttrType(AttrType.attrInteger);
            joinExprs[1].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 7);
            joinExprs[1].operand2.integer = Integer.parseInt(EN[1]);
        }

        //label
        if (ENType[2] == 1) {
            joinExprs[2].op = new AttrOperator(AttrOperator.aopEQ);
            joinExprs[2].next = null;
            joinExprs[2].type1 = new AttrType(AttrType.attrSymbol);
            joinExprs[2].type2 = new AttrType(AttrType.attrString);
            joinExprs[2].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 3);
            joinExprs[2].operand2.string = EN[2];
        }
        // weight
        else {
            joinExprs[2].op = new AttrOperator(AttrOperator.aopLE);
            joinExprs[2].next = null;
            joinExprs[2].type1 = new AttrType(AttrType.attrSymbol);
            joinExprs[2].type2 = new AttrType(AttrType.attrInteger);
            joinExprs[2].operand1.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 4);
            joinExprs[2].operand2.integer = Integer.parseInt(EN[2]);
        }

        joinExprs[3].op = new AttrOperator(AttrOperator.aopEQ);
        joinExprs[3].next = null;
        joinExprs[3].type1 = new AttrType(AttrType.attrSymbol);
        joinExprs[3].type2 = new AttrType(AttrType.attrSymbol);
        joinExprs[3].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 1);
        joinExprs[3].operand2.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 2);

        joinExprs[4].op = new AttrOperator(AttrOperator.aopEQ);
        joinExprs[4].next = null;
        joinExprs[4].type1 = new AttrType(AttrType.attrSymbol);
        joinExprs[4].type2 = new AttrType(AttrType.attrSymbol);
        joinExprs[4].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 3);
        joinExprs[4].operand2.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 1);

        joinExprs[5] = null;
    }


}