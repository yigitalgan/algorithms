import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AetheriaCity {

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        if (args.length < 1) {
            System.err.println("Please provide the XML filename");
            return;
        }

        Document doc = loadXML(args[0]);
        Element root = doc.getDocumentElement();

        // header
        printLine();
        System.out.println("  AETHERIA CITY \u2013 Full System Boot Report");
        printLine();

        // --- PART A ---
        printSection("PART A  |  Power Grid");
        handlePowerGrid(root);

        // --- PART B ---
        printSection("PART B  |  Master Boot Sequence (valid order)");
        handleBootSequence(root);

        // --- PART C ---
        printSection("PART C  |  Secure Communication Hubs (SCCs)");
        String[] stNames = null;
        Map<String, Integer> stMap = null;
        Digraph fiberNet = null;

        Element fiberRoot = getChild(root, "fiberLinks");
        List<Element> stations = getChildren(fiberRoot, "station");
        int numSt = stations.size();
        stNames = new String[numSt];
        stMap = new LinkedHashMap<>();
        for (int i = 0; i < numSt; i++) {
            Element st = stations.get(i);
            stMap.put(getField(st, "id"), i);
            stNames[i] = getField(st, "name");
        }
        fiberNet = new Digraph(numSt);
        for (int i = 0; i < numSt; i++) {
            Element st = stations.get(i);
            int from = stMap.get(getField(st, "id"));
            // stations can have <outgoing> or <links> wrapper
            Element wrapper = getChild(st, "outgoing");
            if (wrapper == null) wrapper = getChild(st, "links");
            if (wrapper == null) continue;
            for (Element lnk : getChildren(wrapper, "link")) {
                Integer to = stMap.get(getField(lnk, "target"));
                if (to != null) fiberNet.addEdge(from, to);
            }
        }
        CommHubs.findHighInteractionZones(fiberNet, stNames);

        // --- PART D ---
        printSection("PART D  |  Bio-Leak Containment Protocol");
        Element leakRoot = getChild(root, "leakScenarios");
        if (leakRoot != null) {
            for (Element sc : getChildren(leakRoot, "scenario")) {
                int srcIdx = stMap.get(getField(sc, "sourceId"));
                System.out.println("Leak origin: " + stNames[srcIdx] + " (id=" + srcIdx + ")");
                System.out.println();

                List<Integer> atRisk = BioLeakContainment.dfsReachable(fiberNet, srcIdx, stNames);
                System.out.println("=== Part D, Task 1 " + stNames[srcIdx] + "(" + srcIdx + ") ===");
                System.out.println("Stations at risk (" + atRisk.size() + "):");
                for (int v : atRisk) System.out.println("  " + stNames[v] + "(" + v + ")");
                System.out.println();

                List<List<Integer>> levels = BioLeakContainment.bfsLayers(fiberNet, srcIdx, stNames);
                System.out.println("=== Part D, Task 2 " + stNames[srcIdx] + "(" + srcIdx + ") ===");
                System.out.println("Evacuation priority layers:");
                for (int d = 0; d < levels.size(); d++) {
                    StringBuilder sb = new StringBuilder();
                    if (d == 0) sb.append("  Layer 0 (source)     : ");
                    else sb.append(String.format("  Layer %d (priority %d): ", d, d));
                    List<Integer> layer = levels.get(d);
                    for (int j = 0; j < layer.size(); j++) {
                        if (j > 0) sb.append(", ");
                        sb.append(stNames[layer.get(j)]).append("(").append(layer.get(j)).append(")");
                    }
                    System.out.println(sb);
                }
            }
        }
    }

    /* ============ Part A helper ============ */
    private static void handlePowerGrid(Element root) {
        Element distRoot = getChild(root, "districts");
        List<Element> districts = getChildren(distRoot, "district");
        int n = districts.size();

        String[] dNames = new String[n];
        Map<String, Integer> dMap = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            Element d = districts.get(i);
            dMap.put(getField(d, "id"), i);
            dNames[i] = getField(d, "name");
        }

        Graph grid = new Graph(n);
        for (int i = 0; i < n; i++) {
            Element d = districts.get(i);
            int u = dMap.get(getField(d, "id"));
            Element linksWrap = getChild(d, "links");
            if (linksWrap == null) continue;
            for (Element lnk : getChildren(linksWrap, "link")) {
                int v = dMap.get(getField(lnk, "target"));
                int w = Integer.parseInt(getField(lnk, "cableCost"));
                if (u < v) grid.addEdge(new Edge(u, v, w));
            }
        }

        boolean allConnected = PowerGrid.isConnected(grid);
        System.out.println("City fully connected? " + (allConnected ? "YES \u2713" : "NO \u2717"));
        System.out.println();

        List<Edge> mst = PowerGrid.kruskalMST(grid);
        System.out.println("Minimum Spanning Tree (" + mst.size() + " cables):");
        int sum = 0;
        for (Edge e : mst) {
            int a = e.either(), b = e.other(a);
            System.out.printf("  %-22s \u2194 %-23s cost: %d%n",
                    dNames[a], dNames[b], (int) e.weight());
            sum += (int) e.weight();
        }
        System.out.println("Total minimum cable cost: " + sum + " units");
    }

    /* ============ Part B helper ============ */
    private static void handleBootSequence(Element root) {
        Element bootRoot = getChild(root, "bootDependencies");
        List<Element> systems = getChildren(bootRoot, "s");
        int n = systems.size();

        String[] sNames = new String[n];
        Map<String, Integer> sMap = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            Element s = systems.get(i);
            sMap.put(getField(s, "id"), i);
            sNames[i] = getField(s, "name");
        }

        Digraph deps = buildDependencyGraph(systems, sMap, n);

        System.out.println("=== Part B: Master Boot Sequence ===");
        List<Integer> order = BootSequence.computeBootSequence(deps, sNames);
        if (order != null) {
            System.out.println("Valid boot sequence found:");
            for (int step = 0; step < order.size(); step++) {
                int id = order.get(step);
                System.out.printf("  Step %2d: %s (id=%d)%n", step + 1, sNames[id], id);
            }
        }

        // deadlock demo
        System.out.println();
        System.out.println("--- Introducing circular dependency (deadlock demo) ---");
        Digraph broken = buildDependencyGraph(systems, sMap, n);
        // reverse an existing edge to force a cycle
        boolean added = false;
        for (int v = 0; v < n && !added; v++) {
            for (int u : broken.adj(v)) {
                broken.addEdge(u, v);
                added = true;
                break;
            }
        }
        System.out.println("=== Part B: Master Boot Sequence ===");
        BootSequence.computeBootSequence(broken, sNames);
    }

    private static Digraph buildDependencyGraph(List<Element> systems, Map<String, Integer> idMap, int n) {
        Digraph g = new Digraph(n);
        for (int i = 0; i < n; i++) {
            Element s = systems.get(i);
            int dependent = idMap.get(getField(s, "id"));
            Element reqWrap = getChild(s, "requires");
            if (reqWrap == null) continue;
            for (Element req : getChildren(reqWrap, "req")) {
                String rid = req.getTextContent().trim();
                if (!rid.isEmpty() && idMap.containsKey(rid)) {
                    g.addEdge(dependent, idMap.get(rid));
                }
            }
        }
        return g;
    }

    /* ============ XML utility methods ============ */

    private static Document loadXML(String name) throws Exception {
        File f = new File(name);
        if (!f.exists()) f = new File(name + ".xml");
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(f);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static String getField(Element el, String tag) {
        if (el == null) return "";
        NodeList nl = el.getElementsByTagName(tag);
        return nl.getLength() > 0 ? nl.item(0).getTextContent().trim() : "";
    }

    private static Element getChild(Element parent, String tag) {
        if (parent == null) return null;
        NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node nd = kids.item(i);
            if (nd.getNodeType() == Node.ELEMENT_NODE && nd.getNodeName().equals(tag))
                return (Element) nd;
        }
        return null;
    }

    private static List<Element> getChildren(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        if (parent == null) return result;
        NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node nd = kids.item(i);
            if (nd.getNodeType() == Node.ELEMENT_NODE && nd.getNodeName().equals(tag))
                result.add((Element) nd);
        }
        return result;
    }

    private static void printLine() {
        System.out.println("\u2550".repeat(60));
    }

    private static void printSection(String title) {
        System.out.println();
        printLine();
        System.out.println("  " + title);
        printLine();
    }
}
