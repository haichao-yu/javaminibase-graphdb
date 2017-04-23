package graphtests;

import global.GlobalConst;
import global.SystemDefs;

import java.io.File;

/**
 * Created by yhc on 3/14/17.
 */

public class GraphDBManager implements GlobalConst {

    private String dbPath;
    private SystemDefs sysdef;

    public GraphDBManager() {}

    public void init(String dbname) {
        dbPath = "tmpDBFile/" + dbname + ".minibase-db";
        // check if the database is already exists
        File file = new File(dbPath);
        if (file.exists()) { // open
            sysdef = new SystemDefs(dbPath, 1000, "Clock");
        }
        else { // create
            sysdef = new SystemDefs(dbPath, 100000, 1000, "Clock");
        }
    }

    public void init(String dbname, int NUMBUF) {
        dbPath = "tmpDBFile/" + dbname + ".minibase-db";
        File file = new File(dbPath);
        if (file.exists()) { // open
            sysdef = new SystemDefs(dbPath, NUMBUF, "Clock");
        }
        else { // create
            sysdef = new SystemDefs(dbPath, 100000, NUMBUF, "Clock");
        }
    }

    public void close() {
        sysdef.close();
    }
}
