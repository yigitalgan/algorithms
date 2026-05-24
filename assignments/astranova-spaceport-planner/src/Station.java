import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Station implements Serializable {
    static final long serialVersionUID = 222L;

    Point p;
    String name;
    List<Edge> edges = new ArrayList<>();

    public Station(Point point, String stationName) {
        p = point;
        name = stationName;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof Station)) return false;
        Station other = (Station) value;
        return Objects.equals(name, other.name) && Objects.equals(p, other.p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, p);
    }
}
