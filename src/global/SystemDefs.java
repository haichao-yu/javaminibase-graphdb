package global;

import bufmgr.*;
import diskmgr.*;
import catalog.*;

public class SystemDefs {
    public static BufMgr JavabaseBM;
    public static DB JavabaseDB;
    public static Catalog JavabaseCatalog;

    public static String JavabaseDBName;
    public static String JavabaseLogName;
    public static boolean MINIBASE_RESTART_FLAG = false;
    public static String MINIBASE_DBNAME;

    public SystemDefs() {}

    /**
     * Create a new DB
     * @param dbname
     * @param num_pgs
     * @param bufpoolsize
     * @param replacement_policy
     */
    public SystemDefs(String dbname, int num_pgs, int bufpoolsize, String replacement_policy) {

        JavabaseDBName = new String(dbname);
        JavabaseLogName = new String(dbname);
        try {
            JavabaseBM = new BufMgr(bufpoolsize, replacement_policy);
            JavabaseDB = new DB();
        } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }

        try {
            JavabaseDB.openDB(dbname, num_pgs);
            JavabaseBM.flushAllPages();
        } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Open a existed DB
     * @param dbname
     * @param bufpoolsize
     * @param replacement_policy
     */
    public SystemDefs(String dbname, int bufpoolsize, String replacement_policy) {

        JavabaseDBName = new String(dbname);
        JavabaseLogName = new String(dbname);

        try {
            JavabaseBM = new BufMgr(bufpoolsize, replacement_policy);
            JavabaseDB = new DB();
        } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }

        try {
            JavabaseDB.openDB(dbname);
        } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Close DB (remember flush all pages, or data won't be written to file)
     */
    public void close() {
        try {
            JavabaseBM.flushAllPages();
            JavabaseDB.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
