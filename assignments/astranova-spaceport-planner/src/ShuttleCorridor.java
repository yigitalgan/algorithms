import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class ShuttleCorridor implements Serializable {
    static final long serialVersionUID = 333L;

    String name;
    List<Station> stations;

    public ShuttleCorridor(String corridorName, List<Station> corridorStations) {
        name = corridorName;
        stations = corridorStations;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof ShuttleCorridor)) return false;
        ShuttleCorridor other = (ShuttleCorridor) value;
        return Objects.equals(name, other.name) && Objects.equals(stations, other.stations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, stations);
    }
}
