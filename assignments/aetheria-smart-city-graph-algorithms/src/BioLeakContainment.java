import java.util.*;

public class BioLeakContainment {

    // finds all stations reachable from the leak source using depth-first search
    public static List<Integer> dfsReachable(Digraph g, int source, String[] labels) {
        boolean[] seen = new boolean[g.V()];
        List<Integer> affected = new ArrayList<>();
        spreadDFS(g, source, seen, affected);
        return affected;
    }

    private static void spreadDFS(Digraph g, int v, boolean[] seen, List<Integer> out) {
        seen[v] = true;
        out.add(v);
        for (int next : g.adj(v)) {
            if (!seen[next]) spreadDFS(g, next, seen, out);
        }
    }

    // BFS from source station, groups reachable stations by hop distance
    public static List<List<Integer>> bfsLayers(Digraph g, int source, String[] labels) {
        int n = g.V();
        int[] hops = new int[n];
        Arrays.fill(hops, -1);
        hops[source] = 0;

        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(source);

        List<List<Integer>> levels = new ArrayList<>();
        levels.add(new ArrayList<>());
        levels.get(0).add(source);

        while (!queue.isEmpty()) {
            int cur = queue.poll();
            for (int next : g.adj(cur)) {
                if (hops[next] == -1) {
                    hops[next] = hops[cur] + 1;
                    queue.add(next);
                    while (levels.size() <= hops[next])
                        levels.add(new ArrayList<>());
                    levels.get(hops[next]).add(next);
                }
            }
        }
        return levels;
    }
}
