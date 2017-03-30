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
 * Edge: Head (12 bytes) + srcNID (8 bytes) + dstNID (8 bytes) + Label (50 bytes) + '\0' (2 bytes) + Weight (4 bytes) = 84 bytes
 */
public class Edge extends Tuple {

    /**
     * the maximum length of edge label
     * (suppose it is 50 bytes)
     */
    public static final int max_length_of_edge_label = 50;

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
    private short[] fldOffset;

    /**
     * Class constructor
     * Create a new edge with length = 84, edge offset = 0.
     */
    public Edge() throws IOException {
        edge_length = 12 + 8 + 8 + max_length_of_edge_label + 2 + 4; // 84
        data = new byte[edge_length];
        edge_offset = 0; // where head begins
        fldCnt = 4;
        fldOffset = new short[fldCnt + 1];
        fldOffset[0] = 12; // where data begins
        fldOffset[1] = (short) (fldOffset[0] + 8); // 20
        fldOffset[2] = (short) (fldOffset[1] + 8); // 28
        fldOffset[3] = (short) (fldOffset[2] + max_length_of_edge_label + 2); // 80
        fldOffset[4] = (short) edge_length; // 84
        // write head to data
        Convert.setShortValue(fldCnt, 0, data);
        Convert.setShortValue(fldOffset[0], 2, data);
        Convert.setShortValue(fldOffset[1], 4, data);
        Convert.setShortValue(fldOffset[2], 6, data);
        Convert.setShortValue(fldOffset[3], 8, data);
        Convert.setShortValue(fldOffset[4], 10, data);
    }

    /**
     * Constructor
     *
     * @param aedge  a byte array which contains the edge
     * @param offset the offset of the edge in the byte array
     */
    public Edge(byte[] aedge, int offset) {
        edge_length = 12 + 8 + 8 + max_length_of_edge_label + 2 + 4; // 84
        data = new byte[edge_length];
        edge_offset = 0; // where head begins
        System.arraycopy(aedge, offset, data, edge_offset, aedge.length - offset);
        fldCnt = 4;
        fldOffset = new short[fldCnt + 1];
        fldOffset[0] = 12; // where data begins
        fldOffset[1] = (short) (fldOffset[0] + 8); // 20
        fldOffset[2] = (short) (fldOffset[1] + 8); // 28
        fldOffset[3] = (short) (fldOffset[2] + max_length_of_edge_label + 2); // 80
        fldOffset[4] = (short) edge_length; // 84
    }

    /**
     * Constructor(used as edge copy)
     *
     * @param fromEdge a byte array which contains the edge
     */
    public Edge(Edge fromEdge) {
        edge_length = 16 + 8 + 8 + max_length_of_edge_label + 2 + 4 + 32 + 32; // 84
        data = fromEdge.getEdgeByteArray();
        edge_offset = 0; // where head begins
        fldCnt = 4;
        fldOffset = new short[fldCnt + 1];
        fldOffset[0] = 12; // where data begins
        fldOffset[1] = (short) (fldOffset[0] + 8); // 20
        fldOffset[2] = (short) (fldOffset[1] + 8); // 28
        fldOffset[3] = (short) (fldOffset[2] + max_length_of_edge_label + 2); // 80
        fldOffset[4] = (short) edge_length; // 84
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