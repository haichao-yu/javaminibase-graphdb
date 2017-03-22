package nodeheap;

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

public class NodeHeapfile extends Heapfile{

	private boolean nodestatus;
	private int NodeCounter;
	private byte[] NodeData;
	private int NodeLength;
	private boolean NodeUpdateStatus;


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
			IOException{
		super.deleteFile();
	}

	// delete Node
	public boolean deleteNode(NID nid) throws InvalidSlotNumberException,
			InvalidTupleSizeException,
			HFException,
			HFBufMgrException,
			HFDiskMgrException,
			Exception
	{
		nodestatus = super.deleteRecord(nid);
		return nodestatus;
	}


	//count Node
	public int getNodeCnt() throws InvalidSlotNumberException,
			InvalidTupleSizeException,
			HFDiskMgrException,
			HFBufMgrException,
			IOException{
		NodeCounter = super.getRecCnt();
		return NodeCounter;
	}


	//Get node
	public Node getNode(NID nid) throws InvalidSlotNumberException,
			InvalidTupleSizeException,
			HFException,
			HFDiskMgrException,
			HFBufMgrException,
			Exception
	{
		Tuple tuple = super.getRecord(nid);
		NodeData = tuple.data;
		NodeLength = tuple.tuple_length;

		// Node offset set 0; data consists of Descriptor and label
		Node getnode = new Node(NodeData,0, NodeLength);

		return getnode;
		//Tuple node = new Tuple();
	}


	// insert node
	public NID insertNode(byte[] nodePtr) throws InvalidSlotNumberException,
			InvalidTupleSizeException,
			SpaceNotAvailableException,
			HFException,
			HFBufMgrException,
			HFDiskMgrException,
			IOException
	{
		RID rid = super.insertRecord(nodePtr);
		NID nid = new NID(rid.pageNo, rid.slotNo);
		return nid;
	}

	// initilize a NScan
	public NScan openScan()
			throws heap.InvalidTupleSizeException,
			IOException
	{
		NScan newnodescan = new NScan(this);
		return newnodescan;
	}


	// Update Node
	public boolean updateNode(NID nid, Node newnode)throws InvalidSlotNumberException,
			InvalidUpdateException,
			InvalidTupleSizeException,
			HFException,
			HFDiskMgrException,
			HFBufMgrException,
			Exception
	{
		NodeUpdateStatus = super.updateRecord(nid,newnode);
		return NodeUpdateStatus;
	}

}