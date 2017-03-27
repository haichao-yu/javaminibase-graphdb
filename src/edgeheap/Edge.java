package edgeheap;

import global.Convert;
import global.NID;

import java.io.IOException;
import java.util.Arrays;

import heap.*;

/**
 * Created by yhc on 2/18/17.
 */

/**
 * srcNID (8 bytes) + dstNID (8 bytes) + Label (100 bytes) + Weight (4 bytes) = 120 bytes
 */
public class Edge extends Tuple {

    /**
     * the maximum length of edge label
     * (suppose it is 100 bytes)
     */
    public static final int max_length_of_edge_label = 100;

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
     * Create a new edge with length = 120, node offset = 0.
     */
    public Edge() {
        // Creat a new edge
        edge_length = 8 + 8 + max_length_of_edge_label + 4; // 120
        data = new byte[edge_length];
        edge_offset = 0;
        fldCnt = 4;
        fldOffset = new short[fldCnt + 1];
        fldOffset[0] = 0;
        fldOffset[1] = 8;
        fldOffset[2] = 16;
        fldOffset[3] = (short) (fldOffset[2] + max_length_of_edge_label);
        fldOffset[4] = (short) edge_length;
    }

    /**
     * Constructor
     *
     * @param aedge  a byte array which contains the edge
     * @param offset the offset of the edge in the byte array
     */
    public Edge(byte[] aedge, int offset) {
        edge_length = 8 + 8 + max_length_of_edge_label + 4; // 120
        data = new byte[edge_length];
        edge_offset = 0;
        System.arraycopy(aedge, offset, data, edge_offset, aedge.length - offset);
        fldCnt = 4;
        fldOffset = new short[fldCnt + 1];
        fldOffset[0] = 0;
        fldOffset[1] = 8;
        fldOffset[2] = 16;
        fldOffset[3] = (short) (fldOffset[2] + max_length_of_edge_label);
        fldOffset[4] = (short) edge_length;
    }

    /**
     * Constructor(used as edge copy)
     *
     * @param fromEdge a byte array which contains the edge
     */
    public Edge(Edge fromEdge) {
        data = fromEdge.getEdgeByteArray();
        edge_offset = 0;
        edge_length = 8 + 8 + max_length_of_edge_label + 4; // 120
        fldCnt = 4;
        fldOffset = new short[fldCnt + 1];
        fldOffset[0] = 0;
        fldOffset[1] = 8;
        fldOffset[2] = 16;
        fldOffset[3] = (short) (fldOffset[2] + max_length_of_edge_label);
        fldOffset[4] = (short) edge_length;
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
     */
    public void edgeInit(byte[] aedge, int offset) {
        System.arraycopy(aedge, offset, data, edge_offset, aedge.length - offset);
    }

    /**
     * Set a edge with the given edge length and offset
     *
     * @param    aedge     a byte array contains the edge
     * @param    offset     the offset of the edge ( =0 by default)
     */
    public void edgeSet(byte[] aedge, int offset) {
        System.arraycopy(aedge, offset, data, edge_offset, aedge.length - offset);
    }

    /**
     * @return size of this edge in bytes
     */
    public short size() {
        return (short) edge_length;
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
        Convert.setNIDValue(sourceID, fldOffset[fldNo - 1], data);
        return this;
    }

    public Edge setDestination(NID destID)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 2;
        Convert.setNIDValue(destID, fldOffset[fldNo - 1], data);
        return this;
    }

    public Edge setLabel(String label)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 3;
        Convert.setStrValue(label, fldOffset[fldNo - 1], data);
        return this;
    }

    public Edge setWeight(int weight)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 4;
        Convert.setIntValue(weight, fldOffset[fldNo - 1], data);
        return this;
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