package global;

/**
 * Created by yhc on 2/14/17.
 */

public class Descriptor {

    private int[] value = new int[5];

    public Descriptor() {
        value[0] = 0;
        value[1] = 0;
        value[2] = 0;
        value[3] = 0;
        value[4] = 0;
    }

    public Descriptor(int value0, int value1, int value2, int value3, int value4) {
        value[0] = value0;
        value[1] = value1;
        value[2] = value2;
        value[3] = value3;
        value[4] = value4;
    }

    public Descriptor(Descriptor d) {
        value[0] = d.get(0);
        value[1] = d.get(1);
        value[2] = d.get(2);
        value[3] = d.get(3);
        value[4] = d.get(4);
    }

    public void set(int value0, int value1, int value2, int value3, int value4) {
        value[0] = value0;
        value[1] = value1;
        value[2] = value2;
        value[3] = value3;
        value[4] = value4;
    }

    public void set(int index, int v) {
        value[index] = v;
    }

    public int get(int idx) {
        return value[idx];
    }

    public boolean equal(Descriptor desc) {
        // return 1 if equal; 0 if not
        for (int i = 0; i < value.length; i++) {
            if (value[i] != desc.value[i]) {
                return false;
            }
        }
        return true;
    }

    public double distance(Descriptor desc) {
        // return the Euclidean distance between the descriptors
        int dist = 0;
        for (int i = 0; i < value.length; i++) {
            dist += ((value[i] - desc.value[i]) * (value[i] - desc.value[i]));
        }
        return Math.sqrt(dist);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(value[0]);
        sb.append(", ");
        sb.append(value[1]);
        sb.append(", ");
        sb.append(value[2]);
        sb.append(", ");
        sb.append(value[3]);
        sb.append(", ");
        sb.append(value[4]);
        sb.append(")");
        return sb.toString();
    }
}
