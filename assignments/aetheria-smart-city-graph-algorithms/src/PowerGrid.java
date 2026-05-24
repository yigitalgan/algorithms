import java.util.*;

public class PowerGrid {

    // checks if all districts can reach each other through cables
    public static boolean isConnected(Graph g) {
        int n = g.V();
        if (n <= 1) return true;

        boolean[] seen = new boolean[n];
        int reached = explore(g, 0, seen);
        return reached == n;
    }

    // recursive walk through the graph, returns how many nodes we touched
    private static int explore(Graph g, int node, boolean[] seen) {
        seen[node] = true;
        int total = 1;
        for (Edge e : g.adj(node)) {
            int neighbor = e.other(node);
            if (!seen[neighbor]) {
                total += explore(g, neighbor, seen);
            }
        }
        return total;
    }

    // finds MST using kruskal - greedy edge selection with union-find
    public static List<Edge> kruskalMST(Graph g) {
        int n = g.V();

        // grab unique edges - since graph stores each edge on both sides,
        // only pick it up from the lower-numbered vertex
        List<Edge> candidates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (Edge e : g.adj(i)) {
                if (i < e.other(i)) {
                    candidates.add(e);
                }
            }
        }
        Collections.sort(candidates); // Edge has compareTo by weight

        // union-find arrays
        int[] par = new int[n];
        int[] sz = new int[n];
        for (int i = 0; i < n; i++) { par[i] = i; sz[i] = 1; }

        List<Edge> tree = new ArrayList<>();
        for (Edge e : candidates) {
            int u = e.either(), v = e.other(u);
            int ru = root(par, u), rv = root(par, v);
            if (ru != rv) {
                // attach smaller tree under bigger one
                if (sz[ru] < sz[rv]) { int tmp = ru; ru = rv; rv = tmp; }
                par[rv] = ru;
                sz[ru] += sz[rv];
                tree.add(e);
                if (tree.size() == n - 1) break;
            }
        }
        return tree;
    }

    // find root with path compression
    private static int root(int[] par, int x) {
        while (par[x] != x) {
            par[x] = par[par[x]]; // path halving
            x = par[x];
        }
        return x;
    }
}
