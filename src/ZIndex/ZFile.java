package ZIndex;

import btree.*;
import global.Descriptor;
import global.NID;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yhc on 3/15/17.
 */

public class ZFile extends BTreeFile {

    private static Descriptor target;
    private static double distance;

    private static ArrayList<NID> result;

    /**
     *
     * @param filename
     * @throws ConstructPageException
     * @throws GetFileEntryException
     * @throws PinPageException
     */
    public ZFile(String filename) throws ConstructPageException, GetFileEntryException, PinPageException {
        super(filename);
    }

    /**
     *
     * @param filename
     * @param keytype
     * @param keysize
     * @param delete_fashion
     * @throws GetFileEntryException
     * @throws ConstructPageException
     * @throws IOException
     * @throws AddFileEntryException
     */
    public ZFile(String filename, int keytype, int keysize, int delete_fashion) throws GetFileEntryException, ConstructPageException, IOException, AddFileEntryException {
        super(filename, keytype, keysize, delete_fashion);
    }

    /**
     *
     * @param key
     * @param dist
     */
    public ArrayList<NID> ZFileRangeScan(DescriptorKey key, double dist) {

        this.target = key.getKey();
        this.distance = dist;
        this.result = new ArrayList<>();

        int d = (int)distance;
        DescriptorKey minimum = new DescriptorKey(key.getKey().get(0) - d, key.getKey().get(1) - d, key.getKey().get(2) - d, key.getKey().get(3) - d, key.getKey().get(4) - d);
        DescriptorKey maximum = new DescriptorKey(key.getKey().get(0) + d, key.getKey().get(1) + d, key.getKey().get(2) + d, key.getKey().get(3) + d, key.getKey().get(4) + d);
        minimum.adjust();
        maximum.adjust();
        some_points_in_rectangle(minimum, maximum);

        return result;
    }

    /**
     *
     * @param minimum
     * @param maximum
     * @return
     */
    private DescriptorKey some_points_in_rectangle(DescriptorKey minimum, DescriptorKey maximum) {

        StringKey lo = new StringKey(minimum.toZValue());
        StringKey hi = new StringKey(maximum.toZValue());

        // open a z scan
        BTFileScan zScan = null;
        try {
            zScan = new_scan(lo, hi);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int misses = 0;
        DescriptorKey last = new DescriptorKey(-1, -1, -1, -1, -1);
        DescriptorKey curr = new DescriptorKey(minimum.getKey().get(0), minimum.getKey().get(1), minimum.getKey().get(2), minimum.getKey().get(3), minimum.getKey().get(4));
        NID currNID = null;
        try {
            KeyDataEntry entry = zScan.get_next();
            if (entry != null) {
                curr.setKey(((StringKey) entry.key).getKey());
                currNID = new NID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
            }
            else {
                return last;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (curr.toZValue().compareTo(maximum.toZValue()) <= 0) { // curr < maximum
            boolean flag = true;
            for (int i = 0; i < 5; i++) {
                if (curr.getKey().get(i) >= minimum.getKey().get(i) && curr.getKey().get(i) <= maximum.getKey().get(i)) {
                    continue;
                }
                else {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                // System.out.println("valid point:   " + curr.toZValue() + " " + curr.toString());
                if (curr.getKey().distance(target) <= distance) {
                    // System.out.println("Found a node!");
                    result.add(currNID);
                }
                misses = 0;
            }
            else {
                // System.out.println("invalid point: " + curr.toZValue() + " " + curr.toString());
                misses++;
            }
            last.setKey(curr.getKey());
            // get next z value
            try {
                KeyDataEntry entry = zScan.get_next();
                if (entry != null) {
                    curr.setKey(((StringKey) entry.key).getKey());
                    currNID = new NID(((LeafData)(entry.data)).getData().pageNo,((LeafData)(entry.data)).getData().slotNo);
                }
                else {
                    return last;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (misses == 5) {
                last.setKey(points_in_rectangle(minimum, maximum, last).getKey());
                break;
            }
        }

        // close the z scan
        try {
            zScan.DestroyBTreeFileScan();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return last;
    }

    /**
     *
     * @param minimum
     * @param maximum
     * @param last
     * @return
     */
    private DescriptorKey points_in_rectangle(DescriptorKey minimum, DescriptorKey maximum, DescriptorKey last) {

        DescriptorKey[] records = zdivide(minimum, maximum);
        DescriptorKey litMax = records[0];
        DescriptorKey bigMin = records[1];
        if (last.toZValue().compareTo(litMax.toZValue()) < 0 && minimum.toZValue().compareTo(litMax.toZValue()) < 0) {
            last.setKey(points_in_rectangle(minimum, litMax, last).getKey());
        }
        else {
            last.setKey(some_points_in_rectangle(bigMin, maximum).getKey());
        }
        if (last.toZValue().compareTo(bigMin.toZValue()) < 0) {
            last.setKey(some_points_in_rectangle(bigMin, maximum).getKey());
        }

        return last;
    }

    /**
     *
     * @param minimum
     * @param maximum
     * @return
     */
    private DescriptorKey[] zdivide(DescriptorKey minimum, DescriptorKey maximum) {

        int[] litMax = new int[5];
        int[] bigMin = new int[5];

        // find the most significant bit of the provided min and max values that differ
        String minZValue = minimum.toZValue();
        String maxZValue = maximum.toZValue();
        int cnt = 0;
        for (int i = 0; i < 80; i++) {
            if (minZValue.charAt(i) == maxZValue.charAt(i)) {
                cnt++;
            }
            else {
                break;
            }
        }

        int significant = cnt % 5;
        for (int i = 0; i < 5; i++) {
            if (i == significant) {
                continue;
            }
            litMax[i] = maximum.getKey().get(i);
            bigMin[i] = minimum.getKey().get(i);
        }
        String minSignificantField = integerToBinary(minimum.getKey().get(significant), 16);
        String maxSignificantField = integerToBinary(maximum.getKey().get(significant), 16);
        StringBuilder litMaxSignficantField = new StringBuilder();
        StringBuilder bigMinSignficantField = new StringBuilder();
        int idx = 0;
        while (idx < 16) {
            if (minSignificantField.charAt(idx) == maxSignificantField.charAt(idx)) {
                litMaxSignficantField.append(maxSignificantField.charAt(idx));
                bigMinSignficantField.append(minSignificantField.charAt(idx));
                idx++;
            }
            else {
                break;
            }
        }
        if (idx < 16) {
            litMaxSignficantField.append('0');
            bigMinSignficantField.append('1');
            idx++;
        }
        while (idx < 16) {
            litMaxSignficantField.append('1');
            bigMinSignficantField.append('0');
            idx++;
        }

        litMax[significant] = Integer.valueOf(litMaxSignficantField.toString(), 2);
        bigMin[significant] = Integer.valueOf(bigMinSignficantField.toString(), 2);
        DescriptorKey[] records = new DescriptorKey[2];
        records[0] = new DescriptorKey(litMax[0], litMax[1], litMax[2], litMax[3], litMax[4]);
        records[1] = new DescriptorKey(bigMin[0], bigMin[1], bigMin[2], bigMin[3], bigMin[4]);

        return records;
    }

    /* ------------------------------ utils methods -------------------------------*/

    /**
     *
     * @param value
     * @param numOfBits
     * @return
     */
    private String integerToBinary(int value, int numOfBits) {
        StringBuilder sb = new StringBuilder();
        int p = 1 << (numOfBits-1);
        for (int i = 0; i < numOfBits; i++) {
            if ((value & p) == 0) {
                sb.append('0');
            }
            else {
                sb.append('1');
            }
            p = p >> 1;
        }
        return sb.toString();
    }
    /* ------------------------------ utils methods -------------------------------*/
}
