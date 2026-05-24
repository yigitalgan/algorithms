import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class LaunchPlan {
    String name;
    List<Operation> ops;

    public LaunchPlan(String planName, List<Operation> operations) {
        name = planName;
        ops = operations;
    }

    public int[] earliest() {
        ScheduleGraph graph = new ScheduleGraph(ops);
        return graph.calculateStarts();
    }

    public int[] getEarliestTimeline() {
        return earliest();
    }

    public int total(int[] schedule) {
        int finish = 0;
        for (Operation op : ops) finish = Math.max(finish, schedule[op.code] + op.time);
        return finish;
    }

    public int getReadinessTime() {
        return total(earliest());
    }

    public static void printLine(int length) {
        for (int i = 0; i < length; i++) System.out.print("-");
        System.out.println();
    }

    public void print() {
        int[] schedule = earliest();
        int width = 65;

        printLine(width);
        System.out.println("Launch Plan: " + name);
        printLine(width);
        System.out.printf("%-8s%-35s%-8s%-8s\n", "Code", "Operation", "Begin", "Finish");
        printLine(width);

        for (Operation op : ops) {
            int start = schedule[op.code];
            System.out.printf("%-8d%-35s%-8d%-8d\n", op.code, op.label, start, start + op.time);
        }

        printLine(width);
        System.out.println("Launch-ready in " + total(schedule) + " hour(s).");
        printLine(width);
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof LaunchPlan)) return false;
        LaunchPlan other = (LaunchPlan) value;
        return Objects.equals(name, other.name) && Objects.equals(ops, other.ops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ops);
    }

    private static class ScheduleGraph {
        private final List<Operation> operations;
        private final Map<Integer, Operation> byCode = new HashMap<>();
        private final Map<Integer, List<Integer>> dependents = new HashMap<>();
        private final Map<Integer, Integer> remainingInputs = new HashMap<>();
        private final int[] startTimes;

        ScheduleGraph(List<Operation> operations) {
            this.operations = operations == null ? new ArrayList<Operation>() : operations;
            int maxCode = 0;
            for (Operation op : this.operations) {
                if (op == null) continue;
                maxCode = Math.max(maxCode, op.code);
                byCode.put(op.code, op);
                dependents.put(op.code, new ArrayList<Integer>());
                remainingInputs.put(op.code, 0);
            }
            startTimes = new int[maxCode + 1];
            wireDependencies();
        }

        private void wireDependencies() {
            for (Operation op : operations) {
                if (op == null || op.prereq == null) continue;
                for (Integer requiredCode : op.prereq) {
                    if (!byCode.containsKey(requiredCode)) continue;
                    dependents.get(requiredCode).add(op.code);
                    remainingInputs.put(op.code, remainingInputs.get(op.code) + 1);
                }
            }
        }

        int[] calculateStarts() {
            Queue<Integer> ready = new PriorityQueue<>();
            for (Operation op : operations) {
                if (op != null && remainingInputs.get(op.code) == 0) ready.add(op.code);
            }

            int completed = 0;
            while (!ready.isEmpty()) {
                int code = ready.poll();
                Operation current = byCode.get(code);
                completed++;
                release(current, ready);
            }

            if (completed != byCode.size()) throw new IllegalStateException("Cycle in launch plan");
            return startTimes;
        }

        private void release(Operation current, Queue<Integer> ready) {
            int finishTime = startTimes[current.code] + current.time;
            for (Integer nextCode : dependents.get(current.code)) {
                startTimes[nextCode] = Math.max(startTimes[nextCode], finishTime);
                int nextInputs = remainingInputs.get(nextCode) - 1;
                remainingInputs.put(nextCode, nextInputs);
                if (nextInputs == 0) ready.add(nextCode);
            }
        }
    }
}
