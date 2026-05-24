import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LaunchOperationsTimeline {

    public List<LaunchPlan> readXML(String file) {
        List<LaunchPlan> plans = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setCoalescing(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(file));
            document.getDocumentElement().normalize();

            NodeList planNodes = document.getElementsByTagName("LaunchPlan");
            for (int i = 0; i < planNodes.getLength(); i++) {
                Element planElement = (Element) planNodes.item(i);
                plans.add(readPlan(planElement));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return plans;
    }

    private LaunchPlan readPlan(Element planElement) {
        String planName = textOf(planElement, "PlanName");
        List<Operation> operations = new ArrayList<>();
        Element operationBlock = firstElement(planElement, "Operations");
        NodeList operationNodes = operationBlock == null
                ? planElement.getElementsByTagName("Operation")
                : operationBlock.getElementsByTagName("Operation");

        for (int i = 0; i < operationNodes.getLength(); i++) {
            operations.add(readOperation((Element) operationNodes.item(i)));
        }

        return new LaunchPlan(planName, operations);
    }

    private Operation readOperation(Element operationElement) {
        int code = Integer.parseInt(textOf(operationElement, "Code"));
        String label = textOf(operationElement, "Label");
        int executionTime = Integer.parseInt(textOf(operationElement, "ExecutionTime"));
        return new Operation(code, label, executionTime, readPrerequisites(operationElement));
    }

    private List<Integer> readPrerequisites(Element operationElement) {
        List<Integer> prerequisites = new ArrayList<>();
        Element blockedBy = firstElement(operationElement, "BlockedBy");
        if (blockedBy == null) return prerequisites;

        NodeList nodes = blockedBy.getElementsByTagName("RequiresOperation");
        for (int i = 0; i < nodes.getLength(); i++) {
            String text = nodes.item(i).getTextContent().trim();
            if (!text.isEmpty()) prerequisites.add(Integer.parseInt(text));
        }
        return prerequisites;
    }

    private Element firstElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        return (Element) nodes.item(0);
    }

    private String textOf(Element parent, String tagName) {
        Element child = firstElement(parent, tagName);
        return child == null ? "" : child.getTextContent().trim();
    }

    public void printTimeline(List<LaunchPlan> plans) {
        for (LaunchPlan plan : plans) plan.print();
    }
}
