import java.util.List;

/*
 * Main -- entry point for the MediPlan Intelligent Diagnostic Planner.
 *
 * Accepts two command-line arguments:
 *   1. Path to diagnostic_catalogue.xml
 *   2. Path to diagnostic_requests.xml
 *
 * Orchestrates the five processing steps in sequence.
 * The main method signature must not be altered.
 */
public class Main {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: java Main <diagnostic_catalogue.xml> <diagnostic_requests.xml>");
            System.exit(1);
        }

        String cataloguePath = args[0];
        String requestsPath  = args[1];

        // Step 1: Parse catalogue XML and build the test dependency graph
        DiagnosticCatalogue catalogue = new DiagnosticCatalogue();
        catalogue.loadFromXML(cataloguePath);

        // Step 2: Calculate processing costs for all DERIVED tests
        catalogue.computeDerivedCosts();

        // Load the request file to retrieve target identifiers
        DiagnosticRequest request   = DiagnosticRequest.loadFromXML(requestsPath);
        String       singleTarget   = request.getSingleTarget();
        List<String> allTargets     = request.getAllTargets();

        // Step 3: Top-down memoized DP to compute patient sample burden
        MediPlanDP planner = new MediPlanDP(catalogue);
        planner.computeBurdenTopDown(singleTarget);

        // Step 4: Bottom-up tabulation DP to determine naive hospital cost
        planner.computeCostBottomUp(allTargets);

        // Step 5: Traceback to produce the combined plan with optimized cost
        planner.buildDiagnosticPlan(singleTarget);
    }
}
