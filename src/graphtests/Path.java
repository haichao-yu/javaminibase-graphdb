package graphtests;

/**
 * Created by yhc on 4/13/17.
 */

public class Path implements Comparable<Path> {

    private String head;
    private String tail;

    public Path(String h, String t) {
        head = h;
        tail = t;
    }

    @Override
    public int compareTo(Path o) {

        // sort by head first
        int headCompareResult = this.head.compareTo(o.head);
        if (headCompareResult != 0) {
            return headCompareResult;
        }

        // then sort by tail
        return this.tail.compareTo(o.tail);
    }

    @Override
    public int hashCode() {
        return head.hashCode() * 32713 + tail.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Path)) {
            return false;
        }
        Path path = (Path) obj;
        return (this.head.compareTo(path.head) == 0) && (this.tail.compareTo(path.tail) == 0);
    }

    @Override
    public String toString() {
        return head + " -> " + tail;
    }
}