package edgeheap;

import diskmgr.Page;
import global.NID;
import global.PageId;
import global.RID;
import global.EID;
import global.Convert;
import global.Descriptor;
import heap.*;
import heap.FileAlreadyDeletedException;
import heap.HFBufMgrException;
import heap.HFDiskMgrException;
import heap.HFException;
import heap.InvalidSlotNumberException;
import heap.InvalidTupleSizeException;
import heap.InvalidUpdateException;
import heap.SpaceNotAvailableException;

import java.io.IOException;

public class EdgeHeapfile extends Heapfile{

    public boolean deleteEdge;
    public int getEdgeCnt;
    public Edge getEdge;
    public EID insertEdge;
    public EScan openEdgeScan;
    public boolean updateEdge;
    private boolean edgestatus;
    private int EdgeCounter;
    private byte[] EdgeData;
    private int EdgeLength;
    private boolean EdgeUpdateStatus;

    public EdgeHeapfile(String file) throws heap.HFException,
            heap.HFBufMgrException,
            heap.HFDiskMgrException,
            IOException {
        super(file);
    }

    // delete File
    public void deleteFile() throws heap.InvalidSlotNumberException,
            FileAlreadyDeletedException,
            heap.InvalidTupleSizeException,
            heap.HFBufMgrException,
            heap.HFDiskMgrException,
            IOException{
        super.deleteFile();
    }

    // delete Edge
    public boolean deleteEdge(EID eid) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFException,
            HFBufMgrException,
            HFDiskMgrException,
            Exception
    {
        edgestatus = super.deleteRecord(eid);
        return edgestatus;
    }


    //count Edge
    public int getEdgeCnt() throws heap.InvalidSlotNumberException,
            heap.InvalidTupleSizeException,
            heap.HFDiskMgrException,
            heap.HFBufMgrException,
            IOException{
        EdgeCounter = super.getRecCnt();
        return EdgeCounter;
    }


    //Get edge
    public Edge getEdge(EID eid) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFException,
            HFDiskMgrException,
            HFBufMgrException,
            Exception
    {
        Tuple tuple = super.getRecord(eid);
        EdgeData = tuple.data;
        EdgeLength = tuple.tuple_length;

        // Node offset set 0; data consists of Descriptor and label
        Edge getedge = new Edge(EdgeData,0,EdgeLength);

        return getedge;
    }


    // insert edge
    public EID insertEdge(byte[] edgePtr) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            SpaceNotAvailableException,
            HFException,
            HFBufMgrException,
            HFDiskMgrException,
            IOException
    {
        RID rid = super.insertRecord(edgePtr);
        EID eid = new EID(rid.pageNo, rid.slotNo);
        return eid;
    }

    // initilize a EScan
    public EScan openScan()
            throws heap.InvalidTupleSizeException, IOException
    {
        EScan newEdgeScan = new EScan(this);
        return newEdgeScan;
    }


    // Update Edge
    public boolean updateNode(EID eid, Edge newEdge)throws InvalidSlotNumberException,
            InvalidUpdateException,
            InvalidTupleSizeException,
            HFException,
            HFDiskMgrException,
            HFBufMgrException,
            Exception
    {
        EdgeUpdateStatus = super.updateRecord(eid,newEdge);
        return EdgeUpdateStatus;
    }

}