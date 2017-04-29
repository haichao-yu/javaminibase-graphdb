package iterator;

import bufmgr.BufMgr;
import bufmgr.PageNotReadException;
import edgeheap.Edge;
import global.AttrOperator;
import global.AttrType;
import global.GlobalConst;
import global.TupleOrder;
import heap.Heapfile;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import index.IndexException;

import java.io.IOException;

import static graphtests.Util.createFileScanForEdge;

/**
 * Created by cc on 4/13/17.
 * Take an edgeHeapFile, perform sort merge join with itself
 * sort the outer one based on destination NID (Ascending)
 * sort the inner one based on source NID (Ascending)
 * join these two fields, i.e., DNID1 = SNID2
 * output tuple has 7 fields (NID, NID, NID, String, String, int, int)
 * (SNID1, DNID1, DNID2, label1, label2, weight1, weight2)
 *
 * Need init DB before, and close smj iterator after
 */

public class NewSortMergeJoins extends Iterator implements GlobalConst {
    private AttrType types1[], types2[];
    private int len1 = 4, len2 = 4;
    private Iterator p_i1,        // pointers to the two iterators. If the
            p_i2;               // inputs are sorted, then no sorting is done
    private TupleOrder order;                      // The sorting order.
    private CondExpr OutputFilter[];

    IoBuf backupIO2 = new IoBuf();

    private boolean get_from_in1, get_from_in2;        // state variables for get_next
    private int joinCol1 = 2, joinCol2 = 1;
    private boolean process_next_block;
    private short strSize2[];
    private IoBuf io_buf1, io_buf2;
    private Tuple TempTuple1, TempTuple2;
    private Tuple tuple1, tuple2;
    private boolean done;
    private byte _bufs1[][], _bufs2[][];
    private int _n_pages;
    private Heapfile temp_file_fd1, temp_file_fd2;
    private AttrType sortFldType;
    private int t1_size, t2_size;
    private Tuple outputTuple;
    private FldSpec proj_list[];
    private int nOutFlds = 7;

    /**
     * constructor,initialization
     * @param edgeFileName the edgeHeapFile to be joined
     * @throws JoinNewFailed       allocate failed
     * @throws JoinLowMemory       memory not enough
     * @throws SortException       exception from sorting
     * @throws TupleUtilsException exception from using tuple utils
     * @throws IOException         some I/O fault
     */
    public NewSortMergeJoins(String edgeFileName)
            throws JoinNewFailed,
            JoinLowMemory,
            SortException,
            TupleUtilsException,
            IOException

    {
        order = new TupleOrder(TupleOrder.Ascending);
        int amt_of_mem = 100;

        // AttrTypes of outer and inner
        types1 = new AttrType[4];
        types1[0] = new AttrType(AttrType.attrNID);
        types1[1] = new AttrType(AttrType.attrNID);
        types1[2] = new AttrType(AttrType.attrString);
        types1[3] = new AttrType(AttrType.attrInteger);
        types2 = new AttrType[4];
        types2[0] = new AttrType(AttrType.attrNID);
        types2[1] = new AttrType(AttrType.attrNID);
        types2[2] = new AttrType(AttrType.attrString);
        types2[3] = new AttrType(AttrType.attrInteger);

        // String size of outer, inner and output
        short[] strSize1 = new short[1];
        strSize1[0] = Edge.max_length_of_edge_label;
        strSize2 = new short[1];
        strSize2[0] = Edge.max_length_of_edge_label;
        short[] outputStrSize = new short[2];
        outputStrSize[0] = Edge.max_length_of_edge_label;
        outputStrSize[1] = Edge.max_length_of_edge_label;

        // AttrTypes of the output
        AttrType[] outputTypes = new AttrType[nOutFlds];
        outputTypes[0] = new AttrType(AttrType.attrNID);
        outputTypes[1] = new AttrType(AttrType.attrNID);
        outputTypes[2] = new AttrType(AttrType.attrNID);
        outputTypes[3] = new AttrType(AttrType.attrString);
        outputTypes[4] = new AttrType(AttrType.attrString);
        outputTypes[5] = new AttrType(AttrType.attrInteger);
        outputTypes[6] = new AttrType(AttrType.attrInteger);

        // what fields to be projected to the output
        proj_list = new FldSpec[nOutFlds];
        RelSpec outer = new RelSpec(RelSpec.outer);
        RelSpec inner = new RelSpec(RelSpec.innerRel);
        proj_list[0] = new FldSpec(outer, 1);
        proj_list[1] = new FldSpec(outer, 2);
        proj_list[2] = new FldSpec(inner, 2);
        proj_list[3] = new FldSpec(outer, 3);
        proj_list[4] = new FldSpec(inner, 3);
        proj_list[5] = new FldSpec(outer, 4);
        proj_list[6] = new FldSpec(inner, 4);

        // Setup the output tuple
        outputTuple = new Tuple();

        short[] ts_size = null;
        try {
            ts_size = TupleUtils.setup_op_tuple(outputTuple, outputTypes,
                    types1, len1, types2, len2,
                    strSize1, strSize2,
                    proj_list, nOutFlds);
        } catch (Exception e) {
            throw new TupleUtilsException(e, "Exception is caught by SortMerge.java");
        }

        // createFileScanForEdge and then sort
        p_i1 = (FileScan) createFileScanForEdge(edgeFileName);
        p_i2 = (FileScan) createFileScanForEdge(edgeFileName);
        try {
            p_i1 = new Sort(types1, (short) len1, strSize1, p_i1, 2,
                        order, Edge.max_length_of_edge_label, amt_of_mem / 2);
        } catch (Exception e) {
            throw new SortException(e, "Sort failed");
        }
        try {
            p_i2 = new Sort(types2, (short) len2, strSize2, p_i2, 1,
                    order, Edge.max_length_of_edge_label, amt_of_mem / 2);
        } catch (Exception e) {
            throw new SortException(e, "Sort failed");
        }

        // Condition outer.DNID = inner.SNID
        OutputFilter = new CondExpr[2];
        OutputFilter[0] = new CondExpr();
        OutputFilter[0].next = null;
        OutputFilter[0].op = new AttrOperator(AttrOperator.aopEQ);
        OutputFilter[0].type1 = new AttrType(AttrType.attrSymbol);
        OutputFilter[0].type2 = new AttrType(AttrType.attrSymbol);
        OutputFilter[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 2);
        OutputFilter[0].operand2.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), 1);
        OutputFilter[1] = null;

        get_from_in1 = true;
        get_from_in2 = true;

        // open io_bufs: used for output blocks
        io_buf1 = new IoBuf();
        io_buf2 = new IoBuf();

        // Allocate memory for the temporary tuples
        TempTuple1 = new Tuple();
        TempTuple2 = new Tuple();
        tuple1 = new Tuple();
        tuple2 = new Tuple();

        if (io_buf1 == null || io_buf2 == null ||
                TempTuple1 == null || TempTuple2 == null ||
                tuple1 == null || tuple2 == null)
            throw new JoinNewFailed("SortMerge.java: allocate failed");
        if (amt_of_mem < 2)
            throw new JoinLowMemory("SortMerge.java: memory not enough");

        try {
            TempTuple1.setHdr((short) len1, types1, strSize1);
            tuple1.setHdr((short) len1, types1, strSize1);
            TempTuple2.setHdr((short) len2, types2, strSize2);
            tuple2.setHdr((short) len2, types2, strSize2);
        } catch (Exception e) {
            throw new SortException(e, "Set header failed");
        }
        t1_size = tuple1.size();
        t2_size = tuple2.size();

        process_next_block = true;
        done = false;

        // Two buffer pages to store equivalence classes
        // NOTE -- THESE PAGES ARE NOT OBTAINED FROM THE BUFFER POOL
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        _n_pages = 10;
        _bufs1 = new byte[_n_pages][MINIBASE_PAGESIZE];
        _bufs2 = new byte[_n_pages][MINIBASE_PAGESIZE];


        temp_file_fd1 = null;
        temp_file_fd2 = null;
        try {
            temp_file_fd1 = new Heapfile(null);
            temp_file_fd2 = new Heapfile(null);
        } catch (Exception e) {
            throw new SortException(e, "Create heap file failed");
        }

        sortFldType = types1[joinCol1 - 1];

        // Now, that stuff is setup, all we have to do is a get_next !!!!
    }

    /**
     * The tuple is returned
     * All this function has to do is to get 1 tuple from one of the Iterators
     * (from both initially), use the sorting order to determine which one
     * gets sent up. Amit)
     * Hmmm it seems that some thing more has to be done in order to account
     * for duplicates.... => I am following Raghu's 564 notes in order to
     * obtain an algorithm for this merging. Some funda about
     * "equivalence classes"
     *
     * @return the joined tuple is returned
     * @throws IOException               I/O errors
     * @throws JoinsException            some join exception
     * @throws IndexException            exception from super class
     * @throws InvalidTupleSizeException invalid tuple size
     * @throws InvalidTypeException      tuple type not valid
     * @throws PageNotReadException      exception from lower layer
     * @throws TupleUtilsException       exception from using tuple utilities
     * @throws PredEvalException         exception from PredEval class
     * @throws SortException             sort exception
     * @throws LowMemException           memory error
     * @throws UnknowAttrType            attribute type unknown
     * @throws UnknownKeyTypeException   key type unknown
     * @throws Exception                 other exceptions
     */

    public Tuple get_next()
            throws IOException,
            JoinsException,
            IndexException,
            InvalidTupleSizeException,
            InvalidTypeException,
            PageNotReadException,
            TupleUtilsException,
            PredEvalException,
            SortException,
            LowMemException,
            UnknowAttrType,
            UnknownKeyTypeException,
            Exception {

        double comp_res;
        Tuple _tuple1, _tuple2;
        if (done) return null;

        while (true) {
            if (process_next_block) {
                process_next_block = false;
                if (get_from_in1)
                    if ((tuple1 = p_i1.get_next()) == null) {
                        done = true;
                        while((tuple2 = p_i2.get_next()) != null);
                        return null;
                    }
                if (get_from_in2)
                    if ((tuple2 = p_i2.get_next()) == null) {
                        done = true;
                        while((tuple1 = p_i1.get_next()) != null);
                        return null;
                    }
                get_from_in1 = get_from_in2 = false;

                // Note that depending on whether the sort order
                // is ascending or descending,
                // this loop will be modified.
                // loop till the join field of 1 >= 2
                comp_res = TupleUtils.CompareTupleWithTuple(sortFldType, tuple1,
                        joinCol1, tuple2, joinCol2);
                while ((comp_res < 0 && order.tupleOrder == TupleOrder.Ascending) ||
                        (comp_res > 0 && order.tupleOrder == TupleOrder.Descending)) {
                    if ((tuple1 = p_i1.get_next()) == null) {
                        done = true;
                        return null;
                    }
                    comp_res = TupleUtils.CompareTupleWithTuple(sortFldType, tuple1,
                            joinCol1, tuple2, joinCol2);
                }

                // loop until the join field of 1 <=2
                comp_res = TupleUtils.CompareTupleWithTuple(sortFldType, tuple1,
                        joinCol1, tuple2, joinCol2);
                while ((comp_res > 0 && order.tupleOrder == TupleOrder.Ascending) ||
                        (comp_res < 0 && order.tupleOrder == TupleOrder.Descending)) {
                    if ((tuple2 = p_i2.get_next()) == null) {
                        done = true;
                        return null;
                    }
                    comp_res = TupleUtils.CompareTupleWithTuple(sortFldType, tuple1,
                            joinCol1, tuple2, joinCol2);
                }

                if (comp_res != 0) {
                    process_next_block = true;
                    continue;
                }

                TempTuple1.tupleCopy(tuple1);
                TempTuple2.tupleCopy(tuple2);

                io_buf1.init(_bufs1, _n_pages, t1_size, temp_file_fd1);
                io_buf2.init(_bufs2, _n_pages, t2_size, temp_file_fd2);
                backupIO2.init(_bufs2, _n_pages, t2_size, temp_file_fd2);

                while (TupleUtils.CompareTupleWithTuple(sortFldType, tuple1,
                        joinCol1, TempTuple1, joinCol1) == 0) {
                    // Insert tuple1 into io_buf1
                    try {
                        io_buf1.Put(tuple1);
                    } catch (Exception e) {
                        throw new JoinsException(e, "IoBuf error in sortmerge");
                    }
                    if ((tuple1 = p_i1.get_next()) == null) {
                        get_from_in1 = true;
                        break;
                    }
                }

                while (TupleUtils.CompareTupleWithTuple(sortFldType, tuple2,
                        joinCol2, TempTuple2, joinCol2) == 0) {
                    // Insert tuple2 into io_buf2

                    try {
                        io_buf2.Put(tuple2);
                        backupIO2.Put(tuple2);
                    } catch (Exception e) {
                        throw new JoinsException(e, "IoBuf error in sortmerge");
                    }
                    if ((tuple2 = p_i2.get_next()) == null) {
                        get_from_in2 = true;
                        break;
                    }
                }

                // tuple1 and tuple2 contain the next tuples to be processed after this set.
                // Now perform a join of the tuples in io_buf1 and io_buf2.
                // This is going to be a simple nested loops join with no frills. I guess,
                // it can be made more efficient, this can be done by a future 564 student.
                // Another optimization that can be made is to choose the inner and outer
                // by checking the number of tuples in each equivalence class.

                if ((_tuple1 = io_buf1.Get(TempTuple1)) == null)                // Should not occur
                    System.out.println("Equiv. class 1 in sort-merge has no tuples");
            }

            if ((_tuple2 = io_buf2.Get(TempTuple2)) == null) {
                if ((_tuple1 = io_buf1.Get(TempTuple1)) == null) {
                    process_next_block = true;
                    while(backupIO2.Get(TempTuple2)!=null);
                    continue;                                // Process next equivalence class
                } else {
                   io_buf2 = new IoBuf(backupIO2);
                    _tuple2 = io_buf2.Get(TempTuple2);
                }
            }
            if (PredEval.Eval(OutputFilter, TempTuple1, TempTuple2, types1, types2) == true) {
                Projection.Join(TempTuple1, types1,
                        TempTuple2, types2,
                        outputTuple, proj_list, nOutFlds);
                return outputTuple;
            }
        }
    }

    /**
     * implement the abstract method close() from super class Iterator
     * to finish cleaning up
     *
     * @throws IOException    I/O error from lower layers
     * @throws JoinsException join error from lower layers
     * @throws IndexException index access error
     */
    public void close()
            throws JoinsException,
            IOException,
            IndexException {
        if (!closeFlag) {
            try {
                p_i1.close();
            } catch (Exception e) {
                throw new JoinsException(e, "SortMerge.java: error in closing iterator.");
            }
            try {
                p_i2.close();
            } catch (Exception e) {
                throw new JoinsException(e, "SortMerge.java: error in closing iterator.");
            }
            if (temp_file_fd1 != null) {
                try {
                    temp_file_fd1.deleteFile();
                } catch (Exception e) {
                    throw new JoinsException(e, "SortMerge.java: delete file failed");
                }
                temp_file_fd1 = null;
            }
            if (temp_file_fd2 != null) {
                try {
                    temp_file_fd2.deleteFile();
                } catch (Exception e) {
                    throw new JoinsException(e, "SortMerge.java: delete file failed");
                }
                temp_file_fd2 = null;
            }
            closeFlag = true;
        }
    }

}