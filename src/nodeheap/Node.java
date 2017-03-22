package nodeheap;

import global.Convert;
import global.Descriptor;
import java.io.IOException;
import heap.Tuple;

/**
 * Created by yhc on 2/16/17.
 */

public class Node extends Tuple implements Comparable {

    /**
     * Maximum size of any tuple
     */
    public static final int max_size = MINIBASE_PAGESIZE;

    /**
     * a byte array to hold data
     */
    private byte[] data;

    /**
     * start position of this node in data[]
     */
    private int node_offset;

    /**
     * length of this node
     */
    private int node_length;

    /**
     * private field
     * Number of fields in this node
     * 2 fixed field: "Label" and "Descriptor"
     */
    private short fldCnt;

    /**
     * private field
     * Array of offsets of the fields
     */
    private short[] fldOffset;

    /**
     * Class constructor
     * Creat a new node with length = max_size, node offset = 0.
     */
    public Node() {
        // Creat a new node
        data = new byte[max_size];
        node_offset = 0;
        node_length = max_size;
        fldOffset = new short[3];
    }

    /**
     * Constructor
     *
     * @param anode  a byte array which contains the node
     * @param offset the offset of the node in the byte array
     * @param length the length of the node
     */
    public Node(byte[] anode, int offset, int length) {
        data = new byte[length];
        System.arraycopy(anode, offset, data, offset, length);
        node_offset = 0;
        node_length = length;
        fldOffset = new short[3];
        fldOffset[0] = 0;
        fldOffset[1] = (short) (length - 20);
        fldOffset[2] = (short) length;
    }

    /**
     * Constructor(used as node copy)
     *
     * @param fromNode a byte array which contains the node
     */
    public Node(Node fromNode) {
        data = fromNode.getNodeByteArray();
        node_length = fromNode.getLength();
        node_offset = 0;
        fldCnt = fromNode.noOfFlds();
        fldOffset = fromNode.copyFldOffset();
    }

    /**
     * Copy a node to the current node position
     * you must make sure the node lengths must be equal
     *
     * @param fromNode the node being copied
     */
    public void nodeCopy(Node fromNode) {
        byte[] temparray = fromNode.getNodeByteArray();
        System.arraycopy(temparray, 0, data, node_offset, node_length);
    }

    /**
     * This is used when you don't want to use the constructor
     *
     * @param anode a byte array which contains the node
     * @param offset the offset of the node in the byte array
     * @param length the length of the node
     */
    public void nodeInit(byte[] anode, int offset, int length) {
        data = anode;
        node_offset = offset;
        node_length = length;
    }

    /**
     * Set a node with the given node length and offset
     *
     * @param    record     a byte array contains the node
     * @param    offset     the offset of the node ( =0 by default)
     * @param    length     the length of the node
     */
    public void nodeSet(byte[] record, int offset, int length) {
        System.arraycopy(record, offset, data, 0, length);
        node_offset = 0;
        node_length = length;
    }

    /**
     * get the length of a node, call this method if you did not
     * call setHdr () before
     *
     * @return length of this node in bytes
     */
    public int getLength() {
        return node_length;
    }

    /**
     * get the length of a node, call this method if you did
     * call setHdr () before
     *
     * @return size of this node in bytes
     */
    public short size() {
        return ((short) (fldOffset[fldCnt] - node_offset));
    }

    /**
     * get the offset of a node
     *
     * @return offset of the node in byte array
     */
    public int getOffset() {
        return node_offset;
    }

    /**
     * Copy the node byte array out
     *
     * @return byte[], a byte array contains the node
     * the length of byte[] = length of the node
     */
    public byte[] getNodeByteArray() {
        byte[] nodecopy = new byte[node_length];
        System.arraycopy(data, node_offset, nodecopy, 0, node_length);
        return nodecopy;
    }

    public String getLabel()
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 1;
        String val = Convert.getStrValue(fldOffset[fldNo - 1], data, fldOffset[fldNo] - fldOffset[fldNo - 1]);
        return val;
    }

    public Descriptor getDesc()
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 2;
        Descriptor val = Convert.getDescValue(fldOffset[fldNo - 1], data);
        return val;
    }

    public Node setLabel(String label)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 1;
        fldOffset[0] = 0;
        fldOffset[1] = (short)(label.length() + 2);
        fldOffset[2] = (short)(label.length() + 2 + 20);
        Convert.setStrValue(label, fldOffset[fldNo - 1], data);
        node_length = label.length() + 2 + 20;
        return this;
    }

    public Node setDesc(Descriptor desc)
            throws IOException, FieldNumberOutOfBoundException {
        int fldNo = 2;
        Convert.setDescValue(desc, fldOffset[fldNo - 1], data);
        return this;
    }

    /**
     * Print out the node
     *
     * @Exception IOException I/O exception
     */
    public void print()
            throws IOException {

        System.out.print("[");

        String label = Convert.getStrValue(fldOffset[0], data, fldOffset[1] - fldOffset[0]);
        System.out.print(label);
        System.out.print(", ");

        Descriptor desc = Convert.getDescValue(fldOffset[1], data);
        System.out.print("(");
        for (int i = 0; i < 4; i++) {
            System.out.print(desc.get(i));
            System.out.print(", ");
        }
        System.out.print(desc.get(4));
        System.out.print(")");
        System.out.println("]");
    }

    @Override
    public int compareTo(Object o) {

        Node node = (Node)o;
        String thisLabel = null;
        String thatLabel = null;
        try {
            thisLabel = this.getLabel();
            thatLabel = ((Node) o).getLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thisLabel.compareTo(thatLabel);
    }
}
