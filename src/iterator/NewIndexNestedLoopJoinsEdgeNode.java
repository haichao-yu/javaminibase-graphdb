package iterator;

import btree.*;
import edgeheap.Edge;
import global.AttrType;
import global.NID;
import heap.Tuple;
import index.IndexException;
import nodeheap.Node;
import nodeheap.NodeHeapfile;

import java.io.IOException;

/**
 * Created by yhc on 4/8/17.
 */

/**
 * New Index Nested Loop Join (Edge |X| Node)
 * type == 1: Edge |X| (source,      node_condition) Node;  Project to Edge
 * type == 2: Edge |X| (destination, node_condition) Node;  Project to Node
 */
public class NewIndexNestedLoopJoinsEdgeNode extends Iterator {

    private NodeHeapfile nhf;

    private AttrType[] outer_type;
    private AttrType[] inner_type;
    private int outer_len;
    private int inner_len;
    private short[] outer_str_sizes;
    private short[] inner_str_sizes;
    private Iterator outer;
    private BTreeFile inner_btf;
    private BTFileScan inner;
    private CondExpr[] right_filter;        // node condition (e.g. node_label == given_label, node_descriptor == given_descriptor)
    private FldSpec[] proj_list;
    private int num_out_flds;

    private boolean done;                   // is the join complete
    private boolean get_from_outer;         // if TRUE, a tuple is got from outer
    private int type;                       // if type = 1, node is src node of edge; if type = 2, node is dst node of edge

    private Tuple outer_tuple;
    private Tuple inner_tuple;
    private Tuple join_tuple;

    /**
     * constructor
     * Initialize the two relations which are joined, including relation type,
     *
     * @param in1          Array containing field types of R.
     * @param len_in1      # of columns in R.
     * @param t1_str_sizes shows the length of the string fields.
     * @param in2          Array containing field types of S
     * @param len_in2      # of columns in S
     * @param t2_str_sizes shows the length of the string fields.
     * @param amt_of_mem   IN PAGES
     * @param am1          access method for left i/p to join
     * @param relationName access heapfile for right i/p to join
     * @param outFilter    select expressions
     * @param rightFilter  reference to filter applied on right i/p
     * @param proj_list    shows what input fields go where in the output tuple
     * @param n_out_flds   number of outer relation fileds
     * @throws IOException         some I/O fault
     * @throws NestedLoopException exception from this class
     */

    /**
     * constructor
     * Initialize the join operator (Edge |X| Node)
     *
     * @param nhfile
     * @param btfNodeLabel
     * @param outerIterator
     * @param rightFilter
     * @param type          if type = 1, node is src node of edge; if type = 2, node is dst node of edge
     * @throws IOException
     * @throws NestedLoopException
     */
    public NewIndexNestedLoopJoinsEdgeNode(NodeHeapfile nhfile,
                                           BTreeFile btfNodeLabel,
                                           Iterator outerIterator,
                                           CondExpr[] rightFilter,
                                           int type
    ) throws IOException, NestedLoopException {

        nhf = nhfile;

        outer = outerIterator;
        inner = null;

        // copy right filter
        if (rightFilter == null) {
            right_filter = null;
        } else {
            right_filter = new CondExpr[rightFilter.length];
            for (int i = 0; i < rightFilter.length - 1; i++) {
                right_filter[i] = new CondExpr(rightFilter[i]);
            }
            right_filter[rightFilter.length - 1] = null;
        }

        this.type = type;

        // outer relation is edge
        outer_type = new AttrType[4];
        outer_type[0] = new AttrType(AttrType.attrNID);
        outer_type[1] = new AttrType(AttrType.attrNID);
        outer_type[2] = new AttrType(AttrType.attrString);
        outer_type[3] = new AttrType(AttrType.attrInteger);
        outer_len = 4;
        outer_str_sizes = new short[1];
        outer_str_sizes[0] = Edge.max_length_of_edge_label;

        // inner relation is node
        inner_type = new AttrType[2];
        inner_type[0] = new AttrType(AttrType.attrString);
        inner_type[1] = new AttrType(AttrType.attrDesc);
        inner_len = 2;
        inner_str_sizes = new short[1];
        inner_str_sizes[0] = Node.max_length_of_node_label;

        //output
        AttrType[] join_type = null;
        if (type == 1) { // edge format
            num_out_flds = 4;
            join_type = new AttrType[num_out_flds];
            proj_list = new FldSpec[num_out_flds];
            RelSpec rel = new RelSpec(RelSpec.outer);
            proj_list[0] = new FldSpec(rel, 1);
            proj_list[1] = new FldSpec(rel, 2);
            proj_list[2] = new FldSpec(rel, 3);
            proj_list[3] = new FldSpec(rel, 4);
        } else { // type == 2 // node format
            num_out_flds = 2;
            join_type = new AttrType[num_out_flds];
            proj_list = new FldSpec[num_out_flds];
            RelSpec rel = new RelSpec(RelSpec.innerRel);
            proj_list[0] = new FldSpec(rel, 1);
            proj_list[1] = new FldSpec(rel, 2);
        }

        join_tuple = new Tuple();
        done = false;
        get_from_outer = true;

        try {
            TupleUtils.setup_op_tuple(join_tuple, join_type,
                    outer_type, outer_len, inner_type, inner_len,
                    outer_str_sizes, inner_str_sizes,
                    proj_list, num_out_flds);
        } catch (TupleUtilsException e) {
            throw new NestedLoopException(e, "TupleUtilsException is caught by NestedLoopsJoins.java");
        }

        inner_btf = btfNodeLabel;
    }

    /**
     * @return The joined tuple is returned
     * @throws Exception
     */
    public Tuple get_next()
            throws Exception {
        // This is a DUMBEST form of a join, not making use of any key information...

        if (done)
            return null;

        do {
            // If get_from_outer is true, Get a tuple from the outer, delete
            // an existing scan on the file, and reopen a new scan on the file.
            // If a get_next on the outer returns DONE?, then the nested loops
            //join is done too.
            if (get_from_outer == true) {
                get_from_outer = false;
                if (inner != null)     // If this not the first time,
                {
                    // close scan
                    inner = null;
                }

                if ((outer_tuple = outer.get_next()) == null) {
                    done = true;
                    if (inner != null) {

                        inner = null;
                    }
                    return null;
                }

                Edge edge = new Edge(outer_tuple.getTupleByteArray(), 0);
                StringKey keyLabel = null;
                if (type == 1) {
                    NID srcNID = edge.getSource();
                    keyLabel = new StringKey(nhf.getNode(srcNID).getLabel());
                } else { // type == 2
                    NID dstNID = edge.getDestination();
                    keyLabel = new StringKey(nhf.getNode(dstNID).getLabel());
                }

                try {
                    inner = inner_btf.new_scan(keyLabel, keyLabel);
                } catch (Exception e) {
                    throw new NestedLoopException(e, "openScan failed");
                }
            }  // ENDS: if (get_from_outer == TRUE)

            // The next step is to get a tuple from the inner,
            // while the inner is not completely scanned && there
            // is no match (with pred),get a tuple from the inner.
            while (true) {
                KeyDataEntry entry = inner.get_next();
                NID nid = null;
                if (entry != null) {
                    nid = new NID(((LeafData) (entry.data)).getData().pageNo, ((LeafData) (entry.data)).getData().slotNo);
                    Node node = nhf.getNode(nid);
                    inner_tuple = new Tuple(node.getNodeByteArray(), 0, node.size());
                    inner_tuple.setHdr((short) inner_len, inner_type, inner_str_sizes);
                    if (PredEval.Eval(right_filter, null, inner_tuple, null, inner_type) == true) {
                        Projection.Join(outer_tuple, outer_type,
                                inner_tuple, inner_type,
                                join_tuple, proj_list, num_out_flds);
                        return join_tuple;
                    }
                } else {
                    break;
                }
            }

            // There has been no match. (otherwise, we would have
            //returned from t//he while loop. Hence, inner is
            //exhausted, => set get_from_outer = TRUE, go to top of loop
            get_from_outer = true; // Loop back to top and get next outer tuple.
        } while (true);
    }

    /**
     * implement the abstract method close() from super class Iterator
     * to finish cleaning up
     *
     * @throws IOException    I/O error from lower layers
     * @throws JoinsException join error from lower layers
     * @throws IndexException index access error
     */
    public void close() throws JoinsException, IOException, IndexException {
        if (!closeFlag) {

            try {
                // inner.DestroyBTreeFileScan();
                outer.close();
            } catch (Exception e) {
                throw new JoinsException(e, "NewIndexNestedLoopJoinsEdgeNode.java: error in closing iterator.");
            }
            closeFlag = true;
        }
    }
}