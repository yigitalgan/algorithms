import java.util.Objects;

public class RouteInstruction {
    String a, b;
    double t;
    boolean shuttle;

    public RouteInstruction(String from, String to, double minutes, boolean byShuttle) {
        a = from;
        b = to;
        t = minutes;
        shuttle = byShuttle;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof RouteInstruction)) return false;
        RouteInstruction other = (RouteInstruction) value;
        return Objects.equals(a, other.a)
                && Objects.equals(b, other.b)
                && Math.abs(t - other.t) < 1e-6
                && shuttle == other.shuttle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, Math.round(t * 1_000_000.0), shuttle);
    }

    @Override
    public String toString() {
        return a + " -> " + b + " (" + t + ")";
    }
}
