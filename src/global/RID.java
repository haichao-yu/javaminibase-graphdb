/*  File RID.java   */

package global;

import java.io.*;

/**
 * class RID
 */

public class RID {

    /**
     * public int slotNo
     */
    public int slotNo;

    /**
     * public PageId pageNo
     */
    public PageId pageNo = new PageId();

    /**
     * default constructor of class
     */
    public RID() {
    }

    /**
     * constructor of class
     */
    public RID(PageId pageno, int slotno) {
        pageNo = pageno;
        slotNo = slotno;
    }

    /**
     * make a copy of the given rid
     */
    public void copyRid(RID rid) {
        pageNo = rid.pageNo;
        slotNo = rid.slotNo;
    }

    /**
     * Write the rid into a byte array at offset
     *
     * @param ary    the specified byte array
     * @param offset the offset of byte array to write
     * @throws java.io.IOException I/O errors
     */
    public void writeToByteArray(byte[] ary, int offset)
            throws java.io.IOException {
        Convert.setIntValue(slotNo, offset, ary);
        Convert.setIntValue(pageNo.pid, offset + 4, ary);
    }

    @Override
    public String toString() {
        String str = "(" + pageNo.pid + "," + slotNo + ")";
        return str;
    }

    @Override
    public boolean equals(Object obj) {
        RID rid = (RID) obj;
        return (this.pageNo.pid == rid.pageNo.pid) && (this.slotNo == rid.slotNo);
    }

    @Override
    public int hashCode() {
        return this.pageNo.pid * 100000 + this.slotNo;
    }
}
