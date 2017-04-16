package graphtests;

/**
 * Created by yanzhexu on 15/04/2017.
 */
public class Triangle implements Comparable<Triangle> {

    private String trianNode1;
    private String trianNode2;
    private String trianNode3;

    public Triangle(String node1, String node2, String node3) {
        trianNode1 = node1;
        trianNode2 = node2;
        trianNode3 = node3;
    }

    @Override
    public int compareTo(Triangle o) {

        // sort by first traingle Node
        int node1CompareResult = this.trianNode1.compareTo(o.trianNode1);
        if (node1CompareResult != 0){
            return node1CompareResult;
        }
        else {
            // sort by second traingle node
            int node2CompareResult = this.trianNode2.compareTo(o.trianNode2);
            if (node2CompareResult != 0) {
                return node2CompareResult;
            }

            // sort by third traingle node
            return this.trianNode3.compareTo(o.trianNode3);
        }
    }

    @Override
    public int hashCode() {
        return trianNode1.hashCode() + trianNode2.hashCode() + trianNode3.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Triangle)) {
            return false;
        }
        Triangle triangle = (Triangle) obj;

        // see if 3 triangles equal

        if ((this.trianNode1.compareTo(triangle.trianNode1) == 0) && (this.trianNode2.compareTo(triangle.trianNode2) == 0) && (this.trianNode3.compareTo(triangle.trianNode3)==0))
        {
            return true;
        }else if ((this.trianNode1.compareTo(triangle.trianNode2) == 0) && (this.trianNode2.compareTo(triangle.trianNode3) == 0) && (this.trianNode3.compareTo(triangle.trianNode1)==0)){
            return true;
        }else if ((this.trianNode1.compareTo(triangle.trianNode3) == 0) && (this.trianNode2.compareTo(triangle.trianNode1) == 0) && (this.trianNode3.compareTo(triangle.trianNode2)==0))
        {
            return true;
        }else{
            return false;
        }
    }

    @Override
    public String toString() {
        return trianNode1 + " -> " + trianNode2 + " -> " + trianNode3;
    }
}