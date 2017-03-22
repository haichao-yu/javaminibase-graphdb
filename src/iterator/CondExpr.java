package iterator;

import java.lang.*;
import java.io.*;

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

    // yhc
    public void getDistance() {
        if (type1.toString() == "attrDesc" && type2.toString() == "attrDesc") {
            distance = operand1.desc.distance(operand2.desc);
        }
    }
}

