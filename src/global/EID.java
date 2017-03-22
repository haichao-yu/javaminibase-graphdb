package global;

/**
 * Created by yhc on 2/18/17.
 */
public class EID extends RID {

    /**
     * default constructor of class
     */
    public EID() {
    }

    /**
     * constructor of class
     */
    public EID(PageId pageno, int slotno) {
        pageNo = pageno;
        slotNo = slotno;
    }
}
