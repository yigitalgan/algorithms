import java.util.List;
import java.util.Objects;

public class Operation {
    int code;
    String label;
    int time;
    List<Integer> prereq;

    public Operation(int operationCode, String operationLabel, int duration, List<Integer> dependencies) {
        code = operationCode;
        label = operationLabel;
        time = duration;
        prereq = dependencies;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof Operation)) return false;
        Operation other = (Operation) value;
        return code == other.code
                && time == other.time
                && Objects.equals(label, other.label)
                && Objects.equals(prereq, other.prereq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, label, time, prereq);
    }
}
