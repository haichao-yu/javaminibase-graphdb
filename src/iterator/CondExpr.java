package iterator;

import global.*;

/**
 * This class will hold single select condition
 * It is an element of linked list which is logically
 * connected by OR operators.
 */

public class CondExpr {

    /**
     * Operator like "<"
     */
    public AttrOperator op;

    /**
     * Types of operands, Null AttrType means that operand is not a
     * literal but an attribute name
     */
    public AttrType type1;
    public AttrType type2;

    /**
     * the left operand and right operand
     */
    public Operand operand1;
    public Operand operand2;

    /**
     * Pointer to the next element in linked list
     */
    public CondExpr next;

    public double distance; // yhc

    /**
     * constructor
     */
    public CondExpr() {

        operand1 = new Operand();
        operand2 = new Operand();

        operand1.integer = 0;
        operand2.integer = 0;

        next = null;
    }

    /**
     * copy constructor
     *
     * @param copy
     */
    public CondExpr(CondExpr copy) {

        this.op = new AttrOperator(copy.op.attrOperator);
        this.next = null;
        this.type1 = new AttrType(copy.type1.attrType);
        this.type2 = new AttrType(copy.type2.attrType);

        this.operand1 = new Operand();
        this.operand1.integer = copy.operand1.integer;
        this.operand1.real = copy.operand1.real;
        if (copy.operand1.symbol != null) {
            this.operand1.symbol = new FldSpec(new RelSpec(copy.operand1.symbol.relation.key), copy.operand1.symbol.offset);
        }
        if (copy.operand1.string != null) {
            this.operand1.string = copy.operand1.string;
        }
        if (copy.operand1.rid != null) {
            this.operand1.rid = new RID(new PageId(copy.operand1.rid.pageNo.pid), copy.operand1.rid.slotNo);
        }
        if (copy.operand1.nid != null) {
            this.operand1.nid = new NID(new PageId(copy.operand1.nid.pageNo.pid), copy.operand1.nid.slotNo);
        }
        if (copy.operand1.eid != null) {
            this.operand1.eid = new EID(new PageId(copy.operand1.eid.pageNo.pid), copy.operand1.eid.slotNo);
        }
        if (copy.operand1.desc != null) {
            this.operand1.desc = new Descriptor(copy.operand1.desc);
        }

        this.operand2 = new Operand();
        this.operand2.integer = copy.operand2.integer;
        this.operand2.real = copy.operand2.real;
        if (copy.operand2.symbol != null) {
            this.operand2.symbol = new FldSpec(new RelSpec(copy.operand2.symbol.relation.key), copy.operand2.symbol.offset);
        }
        if (copy.operand2.string != null) {
            this.operand2.string = copy.operand2.string;
        }
        if (copy.operand2.rid != null) {
            this.operand2.rid = new RID(new PageId(copy.operand2.rid.pageNo.pid), copy.operand2.rid.slotNo);
        }
        if (copy.operand2.nid != null) {
            this.operand2.nid = new NID(new PageId(copy.operand2.nid.pageNo.pid), copy.operand2.nid.slotNo);
        }
        if (copy.operand2.eid != null) {
            this.operand2.eid = new EID(new PageId(copy.operand2.eid.pageNo.pid), copy.operand2.eid.slotNo);
        }
        if (copy.operand2.desc != null) {
            this.operand2.desc = new Descriptor(copy.operand2.desc);
        }
    }

    // yhc
    public void getDistance() {
        if (type1.toString() == "attrDesc" && type2.toString() == "attrDesc") {
            distance = operand1.desc.distance(operand2.desc);
        }
    }
}