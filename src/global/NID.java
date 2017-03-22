package global;

/**
 * Created by yhc on 2/16/17.
 */
public class NID extends RID {

    /**
     * default constructor of class
     */
    public NID() {
    }

    /**
     * constructor of class
     */
    public NID(PageId pageno, int slotno) {
        pageNo = pageno;
        slotNo = slotno;
    }
}
