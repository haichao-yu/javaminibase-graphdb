package nodeheap;

import global.NID;
import global.RID;
import heap.*;

import java.io.IOException;

public class NodeHeapfile extends Heapfile {

    public NodeHeapfile(String file) throws HFException,
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

    // delete Node
    public boolean deleteNode(NID nid) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFException,
            HFBufMgrException,
            HFDiskMgrException,
            Exception {
        boolean status = super.deleteRecord(nid);
        return status;
    }

    //count Node
    public int getNodeCnt() throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFDiskMgrException,
            HFBufMgrException,
            IOException {
        int NodeCounter = super.getRecCnt();
        return NodeCounter;
    }

    //Get node
    public Node getNode(NID nid) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            HFException,
            HFDiskMgrException,
            HFBufMgrException,
            Exception {
        Tuple tuple = super.getRecord(nid);
        byte[] NodeData = tuple.data;

        // Node offset set 0; data consists of Descriptor and label
        Node node = new Node(NodeData, 0);

        return node;
    }

    // insert node
    public NID insertNode(byte[] nodePtr) throws InvalidSlotNumberException,
            InvalidTupleSizeException,
            SpaceNotAvailableException,
            HFException,
            HFBufMgrException,
            HFDiskMgrException,
            IOException {
        RID rid = super.insertRecord(nodePtr);
        NID nid = new NID(rid.pageNo, rid.slotNo);
        return nid;
    }

    // initilize a NScan
    public NScan openScan()
            throws InvalidTupleSizeException,
            IOException {
        NScan newnodescan = new NScan(this);
        return newnodescan;
    }

    // Update Node
    public boolean updateNode(NID nid, Node newNode) throws InvalidSlotNumberException,
            InvalidUpdateException,
            InvalidTupleSizeException,
            HFException,
            HFDiskMgrException,
            HFBufMgrException,
            Exception {
        boolean status = super.updateRecord(nid, newNode);
        return status;
    }
}