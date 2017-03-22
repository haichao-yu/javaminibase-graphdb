package ZIndex;

import btree.KeyClass;
import global.Descriptor;

/**
 * Created by yhc on 3/9/17.
 */

/**  DescriptorKey: It extends the KeyClass.
 *   It defines the descriptor Key.
 */

public class DescriptorKey extends KeyClass {

    private Descriptor key;

    /** Class constructor
     *  @param      value0
     *  @param      value1
     *  @param      value2
     *  @param      value3
     *  @param      value4
     */
    public DescriptorKey(int value0, int value1, int value2, int value3, int value4) {
        key = new Descriptor(value0, value1, value2, value3, value4);
    }

    /**
     * Class copy constructor
     */
    public DescriptorKey(Descriptor value) {
        key = new Descriptor(value);
    }

    /** get a copy of the descriptor key
     *  @return the reference of the copy
     */
    public Descriptor getKey()
    {
        return new Descriptor(key);
    }

    /**
     * set the integer key value with descriptor
     * @param value
     */
    public void setKey(Descriptor value)
    {
        key=new Descriptor(value);
    }

    /**
     * set the integer key value with z value
     * @param zValue
     */
    public void setKey(String zValue) {
        for (int i = 0; i < 5; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < zValue.length(); j += 5) {
                sb.append(zValue.charAt(j));
            }
            key.set(i, Integer.valueOf(sb.toString(), 2));
        }
    }

    public void adjust() {
        for (int i = 0; i < 5; i++) {
            if (key.get(i) < 0) {
                key.set(i, 0);
            }
            if (key.get(i) > 10000) {
                key.set(i, 10000);
            }
        }
    }

    /**
     * get the z value from descriptor;
     * suppose all of 5 integer in descriptor take 16 binary bits (0-10000), which means z value will take 80 binary bits;
     *
     * @return zValue
     */
    public String toZValue(){
        int n = 16; // number of binary bits for every integer in descriptor
        StringBuilder zValue = new StringBuilder();
        int p = 1 << (n-1); // "1000000000000000"
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 5; j++) {
                if ((key.get(j) & p) == 0) { // utilize bit operation to get bit
                    zValue.append('0');
                }
                else {
                    zValue.append('1');
                }
            }
            p = p >> 1;
        }
        return zValue.toString();
    }

    public void ZValuePlusOne() {
        String zValue = toZValue();
        char[] zValueArray = zValue.toCharArray();
        int carry = 1;
        int index = zValueArray.length - 1;
        while (carry != 0) {
            if (zValueArray[index] == '0') {
                zValueArray[index] = '1';
                carry = 0;
            }
            else { // zValueArray[index] == '1'
                zValueArray[index] = '0';
            }
            index--;
        }
        zValue = String.valueOf(zValueArray);
        setKey(zValue);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(key.get(0));
        sb.append(", ");
        sb.append(key.get(1));
        sb.append(", ");
        sb.append(key.get(2));
        sb.append(", ");
        sb.append(key.get(3));
        sb.append(", ");
        sb.append(key.get(4));
        sb.append(")");
        return sb.toString();
    }

    /*
    public static void main(String[] args) {
        DescriptorKey k = new DescriptorKey(99, -1, 45, 777, 23);
        k.adjust();
        System.out.println(k.toZValue());
        k.ZValuePlusOne();
        System.out.println(k.toZValue());
    }
    */
}
