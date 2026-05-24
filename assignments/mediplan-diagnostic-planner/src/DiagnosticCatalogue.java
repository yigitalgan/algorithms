import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;
import java.util.*;

/*
 * DiagnosticCatalogue manages the full inventory of diagnostic tests.
 *
 * Responsibilities:
 *   loadFromXML  - reads and parses diagnostic_catalogue.xml into memory
 *   computeDerivedCosts - assigns a processing_cost to each DERIVED test
 *
 * DERIVED test cost equals the count of distinct non-NONE sample types
 * among its direct RAW dependencies. COMPOSITE tests remain at cost zero.
 */
public class DiagnosticCatalogue {

    /*
     * Internal representation of one entry in the catalogue.
     * Field names and types are fixed by the project specification.
     */
    public static class Test {

        public String       id;
        public String       name;
        public String       type;
        public String       sampleType;
        public int          cost;
        public List<String> inputs;

        public Test() {
            inputs = new ArrayList<>();
        }

        public boolean isRaw() {
            return "RAW".equalsIgnoreCase(type);
        }

        public boolean isDerived() {
            return "DERIVED".equalsIgnoreCase(type);
        }

        public boolean isComposite() {
            return "COMPOSITE".equalsIgnoreCase(type);
        }

        @Override
        public String toString() {
            return id + " [" + type + ", cost=" + cost + "]";
        }
    }

    // Registry of all parsed tests, keyed by test ID. Insertion order is preserved.
    private Map<String, Test> tests = new LinkedHashMap<>();

    /*
     * Parses diagnostic_catalogue.xml and fills the internal test registry.
     * Only standard javax.xml.parsers are used; no third-party dependencies.
     */
    public void loadFromXML(String filePath) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document xmlDoc = db.parse(new File(filePath));
            xmlDoc.getDocumentElement().normalize();

            NodeList entries = xmlDoc.getElementsByTagName("test");

            for (int idx = 0; idx < entries.getLength(); idx++) {
                Node n = entries.item(idx);
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;

                Element el = (Element) n;
                Test entry = new Test();

                entry.id   = el.getAttribute("id");
                entry.name = el.getAttribute("name");
                entry.type = el.getAttribute("type");

                String upperType = entry.type.toUpperCase();

                if ("RAW".equals(upperType)) {
                    entry.sampleType = el.getAttribute("sample_type");
                    entry.cost = Integer.parseInt(el.getAttribute("collection_cost"));
                } else {
                    entry.sampleType = null;
                    entry.cost = 0;

                    NodeList childNodes = el.getChildNodes();
                    for (int k = 0; k < childNodes.getLength(); k++) {
                        Node child = childNodes.item(k);
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            Element childEl = (Element) child;
                            if ("input".equals(childEl.getTagName())) {
                                entry.inputs.add(childEl.getAttribute("ref"));
                            }
                        }
                    }
                }

                tests.put(entry.id, entry);
            }

        } catch (Exception e) {
            System.err.println("Error loading catalogue XML: " + e.getMessage());
        }
    }

    /*
     * Iterates over every DERIVED test and sets its processing_cost.
     *
     * The cost rule: walk through each direct RAW input of a DERIVED test,
     * collect all distinct non-NONE sample types, and store the count as cost.
     * COMPOSITE tests are intentionally skipped here.
     */
    public void computeDerivedCosts() {
        System.out.println("##COST COMPUTATION##");

        for (Map.Entry<String, Test> pair : tests.entrySet()) {
            Test t = pair.getValue();

            if (!t.isDerived()) continue;

            Set<String> seenSampleTypes = new LinkedHashSet<>();

            for (String depId : t.inputs) {
                Test dep = tests.get(depId);
                if (dep == null) continue;

                if (dep.isRaw()) {
                    String st = dep.sampleType;
                    if (st != null && !"NONE".equalsIgnoreCase(st)) {
                        seenSampleTypes.add(st.toUpperCase());
                    }
                }
            }

            t.cost = seenSampleTypes.size();
            System.out.printf("%-25s processing_cost: %d%n", t.id, t.cost);
        }

        System.out.println("##COST COMPUTATION COMPLETED##");
        System.out.println();
    }

    // ----- Accessors (do not modify) -----

    public Test getTest(String id) {
        return tests.get(id);
    }

    public Map<String, Test> getAllTests() {
        return Collections.unmodifiableMap(tests);
    }
}
