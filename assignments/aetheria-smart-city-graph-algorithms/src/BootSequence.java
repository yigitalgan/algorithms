import java.util.*;

public class BootSequence {

    // generates a valid activation order for all systems
    // edges go from dependent -> prerequisite (v needs u => v->u)
    // returns null and prints error if there's a deadlock (cycle)
    public static List<Integer> computeBootSequence(Digraph g, String[] labels) {
        int n = g.V();

        // calculate how many edges point TO each vertex
        int[] incoming = new int[n];
        for (int v = 0; v < n; v++) {
            for (int w : g.adj(v)) {
                incoming[w]++;
            }
        }

        // start with vertices that have no incoming edges
        // use reverse ordering so higher-id systems get processed first
        PriorityQueue<Integer> ready = new PriorityQueue<>(Collections.reverseOrder());
        for (int v = 0; v < n; v++) {
            if (incoming[v] == 0) ready.add(v);
        }

        List<Integer> sequence = new ArrayList<>();
        while (!ready.isEmpty()) {
            int current = ready.poll();
            sequence.add(current);
            // removing this vertex might free up its neighbors
            for (int w : g.adj(current)) {
                incoming[w]--;
                if (incoming[w] == 0) ready.add(w);
            }
        }

        // if we couldn't process everyone, there must be a cycle
        if (sequence.size() < n) {
            int[] backEdge = locateCycle(g);
            System.out.println("  [Cycle detected] Edge " + backEdge[0]
                    + " \u2192 " + backEdge[1] + " creates a circular dependency.");
            System.out.println("SYSTEM INFEASIBLE: Circular reference (deadlock) detected!");
            return null;
        }

        return sequence;
    }

    // DFS to find which edge causes the cycle
    private static int[] locateCycle(Digraph g) {
        int n = g.V();
        int[] state = new int[n]; // 0=unvisited, 1=in progress, 2=done
        int[] edge = {-1, -1};
        for (int v = 0; v < n; v++) {
            if (state[v] == 0) {
                if (cycleSearch(g, v, state, edge)) break;
            }
        }
        return edge;
    }

    private static boolean cycleSearch(Digraph g, int v, int[] state, int[] edge) {
        state[v] = 1;
        for (int w : g.adj(v)) {
            if (state[w] == 1) {
                edge[0] = v; edge[1] = w;
                return true;
            }
            if (state[w] == 0 && cycleSearch(g, w, state, edge)) return true;
        }
        state[v] = 2;
        return false;
    }
}
