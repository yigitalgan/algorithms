import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;
import java.util.*;

/*
 * DiagnosticRequest holds the targets extracted from diagnostic_requests.xml.
 *
 * It exposes:
 *   getSingleTarget - the one test ID used by top-down burden analysis (Step 3)
 *   getAllTargets   - the full list of test IDs for bottom-up cost analysis (Step 4)
 */
public class DiagnosticRequest {

    private String       singleTarget;
    private List<String> allTargets;

    private DiagnosticRequest() {}

    /*
     * Factory method that parses the given XML file and returns a
     * fully populated DiagnosticRequest instance.
     */
    public static DiagnosticRequest loadFromXML(String filePath) {
        DiagnosticRequest request = new DiagnosticRequest();
        request.allTargets = new ArrayList<>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filePath));
            doc.getDocumentElement().normalize();

            // Locate the single_target node
            NodeList singleNodes = doc.getElementsByTagName("single_target");
            if (singleNodes.getLength() > 0) {
                Element sEl = (Element) singleNodes.item(0);
                request.singleTarget = sEl.getAttribute("ref");
            }

            // Locate and collect all entries inside all_targets
            NodeList allTargetsList = doc.getElementsByTagName("all_targets");
            if (allTargetsList.getLength() > 0) {
                Element allEl = (Element) allTargetsList.item(0);
                NodeList items = allEl.getChildNodes();
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        Element itemEl = (Element) item;
                        if ("target".equals(itemEl.getTagName())) {
                            request.allTargets.add(itemEl.getAttribute("ref"));
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading requests XML: " + e.getMessage());
        }

        return request;
    }

    public String getSingleTarget() {
        return singleTarget;
    }

    public List<String> getAllTargets() {
        return Collections.unmodifiableList(allTargets);
    }
}
