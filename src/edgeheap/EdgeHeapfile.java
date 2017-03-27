package edgeheap;

import global.RID;
import global.EID;
import heap.*;

import java.io.IOException;

public class EdgeHeapfile extends Heapfile {

    public EdgeHeapfile(String file) throws HFException,
            HFBufMgrException,
            HFDiskMgrException,
            IOException {
        super(file);
    }

    // delete File
    public void deleteFile() throws InvalidSlotNumberException,
            FileAlreadyDeletedException,
            InvalidTupleSizeException,
            HFBufMgrException,
            HFDiskMgrException,
            IOException {
        super.deleteFile();
    }

    // delete Edge
    public boolean deleteEdge(EID eid) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFException,
            HFBufMgrException,
            HFDiskMgrException,
            Exception {
        boolean status = super.deleteRecord(eid);
        return status;
    }

    //count Edge
    public int getEdgeCnt() throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFDiskMgrException,
            HFBufMgrException,
            IOException {
        int EdgeCounter = super.getRecCnt();
        return EdgeCounter;
    }

    //Get edge
    public Edge getEdge(EID eid) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFException,
            HFDiskMgrException,
            HFBufMgrException,
            Exception {
        Tuple tuple = super.getRecord(eid);
        byte[] EdgeData = tuple.data;

        // Edge offset set 0; data consists of srcNID, dstNID, label and weight
        Edge edge = new Edge(EdgeData, 0);

        return edge;
    }

    // insert edge
    public EID insertEdge(byte[] edgePtr) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            SpaceNotAvailableException,
            HFException,
            HFBufMgrException,
            HFDiskMgrException,
            IOException {
        RID rid = super.insertRecord(edgePtr);
        EID eid = new EID(rid.pageNo, rid.slotNo);
        return eid;
    }

    // initilize a EScan
    public EScan openScan()
            throws InvalidTupleSizeException,
            IOException {
        EScan newEdgeScan = new EScan(this);
        return newEdgeScan;
    }

    // Update Edge
    public boolean updateNode(EID eid, Edge newEdge) throws InvalidSlotNumberException,
            InvalidUpdateException,
            InvalidTupleSizeException,
            HFException,
            HFDiskMgrException,
            HFBufMgrException,
            Exception {
        boolean status = super.updateRecord(eid, newEdge);
        return status;
    }
}