import java.util.*;

public class CommHubs {

    // finds strongly connected components using Kosaraju's two-pass method
    // marks groups with 2+ stations as high-interaction zones
    public static void findHighInteractionZones(Digraph g, String[] labels) {
        int n = g.V();

        // pass 1: DFS on reversed graph, record finish times
        Digraph reversed = g.reverse();
        boolean[] marked = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (!marked[v]) fillOrder(reversed, v, marked, stack);
        }

        // pass 2: process vertices in reverse finish order on original graph
        int[] componentId = new int[n];
        Arrays.fill(componentId, -1);
        int compCount = 0;
        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (componentId[v] == -1) {
                labelComponent(g, v, compCount, componentId);
                compCount++;
            }
        }

        // group vertices by their component
        List<List<Integer>> groups = new ArrayList<>();
        for (int i = 0; i < compCount; i++) groups.add(new ArrayList<>());
        for (int v = 0; v < n; v++) groups.get(componentId[v]).add(v);

        // print results
        System.out.println("=== Part C: Secure Communication Hubs ===");
        System.out.println("Total SCCs found: " + compCount);
        int zones = 0;
        for (int i = 0; i < compCount; i++) {
            List<Integer> members = groups.get(i);
            Collections.sort(members);
            StringBuilder line = new StringBuilder();
            line.append("  SCC-").append(i);
            boolean isZone = members.size() > 1;
            if (isZone) {
                line.append(" [HIGH-INTERACTION ZONE]: {");
                zones++;
            } else {
                line.append(": {");
            }
            for (int j = 0; j < members.size(); j++) {
                if (j > 0) line.append(", ");
                line.append(labels[members.get(j)]);
            }
            line.append("}");
            System.out.println(line);
        }
        System.out.println("High-Interaction Zones: " + zones);
    }

    private static void fillOrder(Digraph g, int v, boolean[] marked, Deque<Integer> stack) {
        marked[v] = true;
        for (int next : g.adj(v)) {
            if (!marked[next]) fillOrder(g, next, marked, stack);
        }
        stack.push(v);
    }

    private static void labelComponent(Digraph g, int v, int label, int[] ids) {
        ids[v] = label;
        for (int next : g.adj(v)) {
            if (ids[next] == -1) labelComponent(g, next, label, ids);
        }
    }
}
