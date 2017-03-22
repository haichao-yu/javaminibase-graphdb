package edgeheap;

import global.Convert;
import global.NID;

import java.io.IOException;
import java.util.Arrays;

import heap.Tuple;

/**
 * Created by yhc on 2/18/17.
 */
public class Edge extends Tuple {

    /**
     * Maximum size of any tuple
     */
    public static final int max_size = MINIBASE_PAGESIZE;

    /**
     * a byte array to hold data
     */
    private byte[] data;

    /**
     * start position of this edge in data[]
     */
    private int edge_offset;

    /**
     * length of this edge
     */
    private int edge_length;

    /**
     * private field
     * Number of fields in this edge
     * 4 fixed field: "Source", "Destination", "Label", "Weight"
     */
    private short fldCnt;

    /**
     * private field
     * Array of offsets of the fields
     */
    private short[] fldOffset = new short[5];

    /**
     * Class constructor
     * Creat a new edge with length = max_size, edge offset = 0.
     */
    public Edge() {
        // Creat a new edge
        data = new byte[max_size];
        edge_offset = 0;
        edge_length = max_size;
    }

    /**
     * Constructor
     *
     * @param aedge  a byte array which contains the edge
     * @param offset the offset of the edge in the byte array
     * @param length the length of the edge
     */
    public Edge(byte[] aedge, int offset, int length) {
        data = aedge;
        edge_offset = offset;
        edge_length = length;
        fldOffset = new short[5];
        fldOffset[0] = 0;
        fldOffset[1] = 8;
        fldOffset[2] = 16;
        fldOffset[3] = (short) (length - 4);
        fldOffset[4] = (short) (length);
    }

    /**
     * Constructor(used as edge copy)
     *
     * @param fromEdge a byte array which contains the edge
     */
    public Edge(Edge fromEdge) {
        data = fromEdge.getEdgeByteArray();
        edge_length = fromEdge.getLength();
        edge_offset = 0;
        fldCnt = fromEdge.noOfFlds();
        fldOffset = fromEdge.copyFldOffset();
    }

    /**
     * Copy a edge to the current edge position
     * you must make sure the edge lengths must be equal
     *
     * @param fromEdge the edge being copied
     */
    public void edgeCopy(Edge fromEdge) {
        byte[] temparray = fromEdge.getEdgeByteArray();
        System.arraycopy(temparray, 0, data, edge_offset, edge_length);
    }

    /**
     * This is used when you don't want to use the constructor
     *
     * @param aedge a byte array which contains the edge
     * @param offset the offset of the edge in the byte array
     * @param length the length of the edge
     */
    public void edgeInit(byte[] aedge, int offset, int length) {
        data = aedge;
        edge_offset = offset;
        edge_length = length;
    }

    /**
     * Set a edge with the given edge length and offset
     *
     * @param    record     a byte array contains the edge
     * @param    offset     the offset of the edge ( =0 by default)
     * @param    length     the length of the edge
     */
    public void edgeSet(byte[] record, int offset, int length) {
        System.arraycopy(record, offset, data, 0, length);
        edge_offset = 0;
        edge_length = length;
    }

    /**
     * get the length of a edge, call this method if you did not
     * call setHdr () before
     *
     * @return length of this edge in bytes
     */
    public int getLength() {
        return edge_length;
    }

    /**
     * get the length of a edge, call this method if you did
     * call setHdr () before
     *
     * @return size of this edge in bytes
     */
    public short size() {
        return ((short) (fldOffset[fldCnt] - edge_offset));
    }

    /**
     * get the offset of a edge
     *
     * @return offset of the edge in byte array
     */
    public int getOffset() {
        return edge_offset;
    }

    /**
     * Copy the edge byte array out
     *
     * @return byte[], a byte array contains the edge
     * the length of byte[] = length of the edge
     */
    public byte[] getEdgeByteArray() {
        byte[] edgecopy = new byte[edge_length];
        System.arraycopy(data, edge_offset, edgecopy, 0, edge_length);
        return edgecopy;
    }

    public NID getSource()
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 1;
        NID val = Convert.getNIDValue(fldOffset[fldNo - 1], data);
        return  val;
    }

    public NID getDestination()
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 2;
        NID val = Convert.getNIDValue(fldOffset[fldNo - 1], data);
        return  val;
    }

    public String getLabel()
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 3;
        String val = Convert.getStrValue(fldOffset[fldNo - 1], data, fldOffset[fldNo] - fldOffset[fldNo - 1]);
        return val;
    }

    public int getWeight()
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 4;
        int val = Convert.getIntValue(fldOffset[fldNo - 1], data);
        return val;
    }

    public Edge setSource(NID sourceID)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 1;
        fldOffset[1] = 8;
        Convert.setNIDValue(sourceID, fldOffset[fldNo - 1], data);
        return this;
    }

    public Edge setDestination(NID destID)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 2;
        fldOffset[2] = 16;
        Convert.setNIDValue(destID, fldOffset[fldNo - 1], data);
        return this;
    }

    public Edge setLabel(String label)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 3;
        fldOffset[3] = (short)(16 + label.length() + 2);
        Convert.setStrValue(label, fldOffset[fldNo - 1], data);
        return this;
    }

    public Edge setWeight(int weight)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 4;
        fldOffset[4] = (short) (fldOffset[3] + 4);
        edge_length = fldOffset[4];
        Convert.setIntValue(weight, fldOffset[fldNo - 1], data);
        edge_offset = 0;
        return this;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "data=" + Arrays.toString(data) +
                ", edge_offset=" + edge_offset +
                ", edge_length=" + edge_length +
                ", fldCnt=" + fldCnt +
                ", fldOffset=" + Arrays.toString(fldOffset) +
                '}';
    }

    /**
     * Print out the edge
     *
     * @Exception IOException I/O exception
     */
    public void print()
            throws IOException {

        System.out.print("[");

        NID source = Convert.getNIDValue(fldOffset[0], data);
        System.out.print(source.toString());
        System.out.print(", ");

        NID destination = Convert.getNIDValue(fldOffset[1], data);
        System.out.print(destination.toString());
        System.out.print(", ");

        String label = Convert.getStrValue(fldOffset[2], data, fldOffset[3] - fldOffset[2]);
        System.out.print(label);
        System.out.print(", ");

        int weight = Convert.getIntValue(fldOffset[3], data);
        System.out.print(weight);

        System.out.println("]");
    }
}
