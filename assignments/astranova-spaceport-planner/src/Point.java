import java.io.Serializable;

public class Point implements Serializable {
    static final long serialVersionUID = 22L;

    public int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof Point)) return false;
        Point other = (Point) value;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
