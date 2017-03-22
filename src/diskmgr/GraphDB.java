package diskmgr;

import global.PageId;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by yutang on 3/12/17.
 */
public class GraphDB extends DB{

    private  RandomAccessFile fp;
    private String name;
    private String name_nodeHeap;
    private String name_edgeHeap;
    private String name_bt_node_label;
    private String name_zt_node_desc;
    private String name_bt_edge_label;
    private String name_bt_edge_weight;
    private String name_bt_edge_source;
    private String name_bt_edge_destination;
    private int openFileType;
    private int num_pages;
    private int bits_per_page;


    public static final int MAX_DESC_KEY_SIZE = 16 * 5; //2^13 < 10000, value[5]
    public static final int MAX_STRING_KEY_SIZE = 20;
    public static final int MAX_INT_KEY_SIZE = 4;

    @Override
    public void openDB(String fname)
            throws IOException, InvalidPageNumberException, FileIOException, DiskMgrException
    {
        name = fname;

        // Creaat a random access file
        fp = new RandomAccessFile(fname, "rw");

        PageId pageId = new PageId();
        Page apage = new Page();
        pageId.pid = 0;


        System.out.println("pageID = " + pageId.pid + ", apage = " + apage.toString());

        num_pages = 1;	//temporary num_page value for pinpage to work

        // pinPage(pageId, apage, false /*read disk*/);


        DBFirstPage firstpg = new DBFirstPage();
        firstpg.openPage(apage);
        num_pages = firstpg.getNumDBPages();

        // unpinPage(pageId, false /* undirty*/);
    }

    @Override
    public void openDB(String fname, int num_pgs)
            throws IOException, InvalidPageNumberException, FileIOException, DiskMgrException
    {
        name = new String(fname);
        num_pages = (num_pgs > 2) ? num_pgs : 2;

        File DBfile = new File(name);

        DBfile.delete();

        // Creaat a random access file
        fp = new RandomAccessFile(fname, "rw");

        // Make the file num_pages pages long, filled with zeroes.
        fp.seek((long)(num_pages*MINIBASE_PAGESIZE-1));
        fp.writeByte(0);

        // Initialize space map and directory pages.

        // Initialize the first diskmgr.DB page
        Page apage = new Page();
        PageId pageId = new PageId();
        pageId.pid = 0;
        // pinPage(pageId, apage, true /*no diskIO*/);

        DBFirstPage firstpg = new DBFirstPage(apage);
        firstpg.setNumDBPages(num_pages);
        /**
         * todo: added special fields for GraphDB
         */
        name_nodeHeap = name + "_nodeHeap";
        name_edgeHeap = name + "_edgeHeap";
        name_bt_node_label = name + "_bt_node_label";
        name_zt_node_desc = name + "_zt_node_desc";
        name_bt_edge_label = name + "_bt_edge_label";
        name_bt_edge_weight = name + "_bt_edge_weight";
        name_bt_edge_source = name + "_bt_edge_source";
        name_bt_edge_destination = name + "_bt_edge_destination";




        PageId nodeHF_pageID = new PageId();
        PageId edgeHF_pageID = new PageId();
        PageId bt_n_l_pageID = new PageId();
        PageId zt_n_desc_pageID = new PageId();
        PageId bt_e_l_pageID = new PageId();
        PageId bt_e_w_pageID = new PageId();
        PageId bt_edge_src = new PageId();
        PageId bt_edge_dest = new PageId();


        firstpg.setFileEntry(nodeHF_pageID, name_nodeHeap, 0);
        firstpg.setFileEntry(edgeHF_pageID, name_edgeHeap, 1);
        firstpg.setFileEntry(bt_n_l_pageID,name_bt_node_label, 2);
        firstpg.setFileEntry(zt_n_desc_pageID, name_zt_node_desc, 3);
        firstpg.setFileEntry(bt_e_l_pageID, name_bt_edge_label, 4);
        firstpg.setFileEntry(bt_e_w_pageID, name_bt_edge_weight, 5);
        firstpg.setFileEntry(bt_edge_src, name_bt_edge_source, 6);
        firstpg.setFileEntry(bt_edge_dest, name_bt_edge_destination, 7);


        // unpinPage(pageId, true /*dirty*/);

        // Calculate how many pages are needed for the space map.  Reserve pages
        // 0 and 1 and as many additional pages for the space map as are needed.
        int num_map_pages = (num_pages + bits_per_page -1)/bits_per_page;

        // set_bits(pageId, 1+num_map_pages, 1);
    }


    public String getFileEntryName(int type, String name)
            throws IOException, DiskMgrException
    {
        String res = null;
        // Creaat a random access file
        try {
            RandomAccessFile fp = new RandomAccessFile(name, "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }

        PageId pageId = new PageId();
        Page apage = new Page();
        pageId.pid = 0;


        num_pages = 1;	//temporary num_page value for pinpage to work

        try {
            // pinPage(pageId, apage, false /*read disk*/);
        } catch (Exception e) {
            e.printStackTrace();
        }


        DBFirstPage firstpg = new DBFirstPage();
        firstpg.openPage(apage);
        num_pages = firstpg.getNumDBPages();

        PageId nodeHF_pageID = new PageId();

        switch (type) {
            case graphDBType.n_heap_f:
                res = firstpg.getFileEntry(nodeHF_pageID, 0);
                break;
            case graphDBType.e_heap_f:
                res = firstpg.getFileEntry(nodeHF_pageID, 1);
                break;
            case graphDBType.n_label:
                res = firstpg.getFileEntry(nodeHF_pageID, 2);
                break;
            case graphDBType.n_desc:
                res = firstpg.getFileEntry(nodeHF_pageID, 3);
                break;
            case graphDBType.e_label:
                res = firstpg.getFileEntry(nodeHF_pageID, 4);
                break;
            case graphDBType.e_weight:
                res = firstpg.getFileEntry(nodeHF_pageID, 5);
                break;
            case graphDBType.e_src:
                res = firstpg.getFileEntry(nodeHF_pageID, 6);
                break;
            case graphDBType.e_dest:
                res = firstpg.getFileEntry(nodeHF_pageID, 7);
                break;
        }

        // unpinPage(pageId, false /* undirty*/);

        return res;
    }
}

interface graphDBType
{
    int n_heap_f = 1;
    int e_heap_f = 2;
    int n_label = 3;
    int n_desc = 4;
    int e_label = 5;
    int e_weight = 6;
    int e_src = 7;
    int e_dest = 8;
}
