import java.io.Serializable;
import java.util.Objects;

public class Edge implements Comparable<Edge>, Serializable {
    static final long serialVersionUID = 444L;

    Station to;
    double w;

    public Edge(Station target, double weight) {
        to = target;
        w = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return Double.compare(w, other.w);
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof Edge)) return false;
        Edge other = (Edge) value;
        return Math.abs(w - other.w) < 1e-9 && Objects.equals(to, other.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to, Math.round(w * 1_000_000_000.0));
    }
}
