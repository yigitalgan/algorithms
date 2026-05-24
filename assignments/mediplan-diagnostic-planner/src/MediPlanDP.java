import java.util.*;

/*
 * MediPlanDP carries out all dynamic-programming steps for the MediPlan system.
 *
 * Step 3 : computeBurdenTopDown / computeSamples
 *          Top-down memoized DP -- finds distinct sample types for a target.
 *
 * Step 4 : computeCostBottomUp / topologicalSort / dfsFinish
 *          Bottom-up tabulation DP -- computes the naive hospital cost.
 *
 * Step 5 : buildDiagnosticPlan
 *          Combines Steps 3 and 4 to produce an optimized execution plan.
 *
 * Support: collectReachable -- iterative DFS used by Steps 4 and 5.
 */
public class MediPlanDP {

    // ---- Fixed fields (names and types must not be changed) ----

    private final DiagnosticCatalogue catalogue;

    /*
     * Memoization table for Step 3.
     * Stores the resolved set of non-NONE sample types for each visited test.
     */
    private final Map<String, Set<String>> burdenMemo = new LinkedHashMap<>();

    /*
     * Tabulation table for Step 4.
     * Stores the accumulated hospital cost for each test in the reachable set.
     */
    private final Map<String, Integer> costTable = new LinkedHashMap<>();

    /*
     * Topological ordering produced in Step 4.
     * Retained for reuse during the Step 5 execution-order derivation.
     */
    private List<String> topoOrder = new ArrayList<>();

    // ---- Constructor ----

    public MediPlanDP(DiagnosticCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    // =========================================================================
    // Step 3
    // =========================================================================

    /*
     * Initiates the top-down burden analysis for a given target test.
     * Delegates to computeSamples for the actual recursive work.
     */
    public void computeBurdenTopDown(String targetId) {
        System.out.println("##PATIENT BURDEN ANALYSIS (Top-Down DP)##");

        Set<String> samples = computeSamples(targetId, 0);

        System.out.println("Patient burden for " + targetId + ": "
                + samples.size() + " sample type(s): " + sortedSetString(samples));

        System.out.println("##PATIENT BURDEN ANALYSIS COMPLETED##");
        System.out.println();
    }

    /*
     * Recursively resolves the set of distinct non-NONE sample types
     * reachable from testId. Results are memoized to avoid redundant work.
     *
     * Each call is printed for traceability; memoized hits are labeled accordingly.
     */
    private Set<String> computeSamples(String testId, int depth) {
        String pad = buildIndent(depth);

        System.out.print(pad + "Called computeSamples(" + testId + ")");

        if (burdenMemo.containsKey(testId)) {
            System.out.println(" -> MEMOIZED " + sortedSetString(burdenMemo.get(testId)));
            return burdenMemo.get(testId);
        }

        System.out.println();

        DiagnosticCatalogue.Test t = catalogue.getTest(testId);
        Set<String> gathered = new LinkedHashSet<>();

        if (t.isRaw()) {
            String st = t.sampleType;
            if (st == null || "NONE".equalsIgnoreCase(st)) {
                System.out.println(pad + testId + " -> {} (NONE, not counted)");
            } else {
                gathered.add(st.toUpperCase());
                System.out.println(pad + testId + " -> " + sortedSetString(gathered));
            }
        } else {
            for (String inputId : t.inputs) {
                Set<String> childSamples = computeSamples(inputId, depth + 1);
                gathered.addAll(childSamples);
            }
            System.out.println(pad + testId + " memoized -> " + sortedSetString(gathered));
        }

        burdenMemo.put(testId, gathered);
        return gathered;
    }

    /*
     * Produces an indentation string of (depth * 2) spaces.
     */
    private String buildIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth * 2; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    // =========================================================================
    // Step 4
    // =========================================================================

    /*
     * Runs bottom-up cost tabulation for each test ID in targetIds.
     * A topological sort of all reachable nodes ensures that dependency
     * costs are always available before a node is processed.
     */
    public void computeCostBottomUp(List<String> targetIds) {
        System.out.println("##HOSPITAL COST ANALYSIS (Bottom-Up DP)##");

        Set<String> universe = new LinkedHashSet<>();
        for (String tid : targetIds) {
            collectReachable(tid, universe);
        }

        topoOrder = topologicalSort(universe);

        System.out.println("Topological order: " + String.join(", ", topoOrder));
        System.out.println();

        for (String testId : topoOrder) {
            DiagnosticCatalogue.Test t = catalogue.getTest(testId);
            int total = 0;

            if (t.isRaw()) {
                total = t.cost;
                System.out.printf("Computing %-25s collection_cost = %-6d total: %d%n",
                        testId + ":", t.cost, total);

            } else if (t.isDerived()) {
                StringBuilder formula = new StringBuilder();
                int depTotal = 0;

                for (String inputId : t.inputs) {
                    int inputCost = costTable.getOrDefault(inputId, 0);
                    depTotal += inputCost;
                    if (formula.length() > 0) formula.append(" + ");
                    formula.append(inputCost);
                }

                String display;
                if (t.cost > 0 || t.inputs.isEmpty()) {
                    display = t.cost + (formula.length() > 0 ? " + " + formula : "");
                } else {
                    display = formula.toString();
                }

                total = t.cost + depTotal;
                System.out.printf("Computing %-25s processing_cost = %-15s total: %d%n",
                        testId + ":", display, total);

            } else {
                // COMPOSITE -- no intrinsic cost, only the sum of its dependencies
                StringBuilder formula = new StringBuilder();
                boolean first = true;
                int depTotal = 0;

                for (String inputId : t.inputs) {
                    int inputCost = costTable.getOrDefault(inputId, 0);
                    depTotal += inputCost;
                    if (!first) formula.append(" + ");
                    formula.append(inputCost);
                    first = false;
                }

                total = depTotal;
                System.out.printf("Computing %-25s cost = %-15s total: %d%n",
                        testId + ":", formula.toString(), total);
            }

            costTable.put(testId, total);
        }

        System.out.println("Results:");
        for (String tid : targetIds) {
            int naiveCost = costTable.getOrDefault(tid, 0);
            System.out.printf("  %-28s naive hospital cost: %d%n", tid, naiveCost);
        }

        System.out.println("##HOSPITAL COST ANALYSIS COMPLETED##");
        System.out.println();
    }

    /*
     * Produces a topological ordering of the nodes in the reachable set
     * by running DFS and recording finish times.
     * Dependencies always appear before the nodes that depend on them.
     */
    public List<String> topologicalSort(Set<String> reachable) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> finishStack = new ArrayDeque<>();

        for (String id : reachable) {
            if (!visited.contains(id)) {
                dfsFinish(id, reachable, visited, finishStack);
            }
        }

        List<String> order = new ArrayList<>(finishStack);
        Collections.reverse(order);
        return order;
    }

    /*
     * DFS post-order traversal used by topologicalSort.
     * Pushes each node onto finishStack only after all its reachable
     * dependencies have been fully explored.
     */
    private void dfsFinish(String testId, Set<String> reachable,
            Set<String> visited, Deque<String> finishStack) {
        visited.add(testId);

        DiagnosticCatalogue.Test t = catalogue.getTest(testId);
        if (t != null) {
            for (String inputId : t.inputs) {
                if (reachable.contains(inputId) && !visited.contains(inputId)) {
                    dfsFinish(inputId, reachable, visited, finishStack);
                }
            }
        }

        finishStack.push(testId);
    }

    // =========================================================================
    // Graph utility
    // =========================================================================

    /*
     * Populates reachable with every test ID reachable from startId,
     * including startId itself. Uses iterative DFS to avoid stack overflow
     * on deep dependency graphs.
     */
    private void collectReachable(String startId, Set<String> reachable) {
        Deque<String> stack = new ArrayDeque<>();
        stack.push(startId);

        while (!stack.isEmpty()) {
            String cur = stack.pop();

            if (reachable.contains(cur)) continue;

            reachable.add(cur);

            DiagnosticCatalogue.Test t = catalogue.getTest(cur);
            if (t == null) continue;

            for (String dep : t.inputs) {
                if (!reachable.contains(dep)) {
                    stack.push(dep);
                }
            }
        }
    }

    // =========================================================================
    // Step 5
    // =========================================================================

    /*
     * Constructs and prints the final diagnostic plan for targetId.
     * Merges the sample set from Step 3 with the cost data from Step 4
     * to produce an optimized execution schedule.
     *
     * Returns the optimized total cost.
     */
    public int buildDiagnosticPlan(String targetId) {
        System.out.println("##DIAGNOSTIC PLAN##");

        DiagnosticCatalogue.Test target = catalogue.getTest(targetId);

        System.out.println("Target: " + targetId + " (" + target.name + ")");
        System.out.println();

        Set<String> sampleSet = burdenMemo.getOrDefault(targetId, new LinkedHashSet<>());
        System.out.println("Patient burden:  " + sampleSet.size() + " sample type(s) required");

        List<String> sortedSamples = new ArrayList<>(sampleSet);
        Collections.sort(sortedSamples);

        List<String> procedureLabels = new ArrayList<>();
        for (String st : sortedSamples) {
            procedureLabels.add(procedureLabel(st));
        }
        System.out.println("  Procedures:    " + String.join(", ", procedureLabels));
        System.out.println();

        List<String> execOrder = getExecutionOrder(targetId);

        System.out.println("Execution order (hospital cost optimized):");

        int optimizedCost = 0;
        int step = 1;

        for (String id : execOrder) {
            DiagnosticCatalogue.Test t = catalogue.getTest(id);
            int cost = t.cost;

            String note = "";
            if (t.isRaw() && t.sampleType != null && !"NONE".equalsIgnoreCase(t.sampleType)) {
                note = " <- " + t.sampleType + " sample";
            }

            System.out.printf("  [%2d]  %-28s %-12s added cost: %2d%s%n",
                    step++, id, "[" + t.type + "]", cost, note);

            optimizedCost += cost;
        }

        System.out.println();

        int naiveCost = costTable.getOrDefault(targetId, 0);
        System.out.println("Naive hospital cost:     " + naiveCost);
        System.out.println("Optimized hospital cost: " + optimizedCost);

        System.out.println("##DIAGNOSTIC PLAN COMPLETED##");
        System.out.println();

        return optimizedCost;
    }

    // =========================================================================
    // Provided helpers -- do NOT modify
    // =========================================================================

    /*
     * Returns the execution order for the given target as a List of test IDs.
     * PROVIDED IN FULL -- do NOT modify.
     */
    public List<String> getExecutionOrder(String targetId) {
        Set<String> reachable = new LinkedHashSet<>();
        collectReachable(targetId, reachable);
        List<String> execOrder = new ArrayList<>();
        for (String id : topoOrder) {
            if (reachable.contains(id))
                execOrder.add(id);
        }
        return execOrder;
    }

    private String procedureLabel(String sampleType) {
        switch (sampleType.toUpperCase()) {
            case "BLOOD":  return "BLOOD draw";
            case "URINE":  return "URINE sample";
            case "TISSUE": return "TISSUE biopsy";
            default:       return sampleType;
        }
    }

    /*
     * Returns a sorted, bracket-enclosed string representation of a set.
     * Example: {"URINE", "BLOOD"} -> "[BLOOD, URINE]"
     * PROVIDED IN FULL.
     */
    protected String sortedSetString(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list.toString();
    }
}
