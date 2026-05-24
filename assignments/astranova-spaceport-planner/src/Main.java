import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        System.out.println("=== LAUNCH OPERATIONS REPORT START ===");

        LaunchOperationsTimeline timeline = new LaunchOperationsTimeline();
        List<LaunchPlan> plans = timeline.readXML(args[0]);
        timeline.printTimeline(plans);

        System.out.println("=== LAUNCH OPERATIONS REPORT END ===");
        System.out.println();
        System.out.println("=== TRANSIT PLAN START ===");

        SpaceportTransitNetwork network = new SpaceportTransitNetwork();
        network.readInput(args[1]);

        SpaceportTransitPlanner router = new SpaceportTransitPlanner();
        List<RouteInstruction> route = router.solve(network);
        router.print(route);

        System.out.println("=== TRANSIT PLAN END ===");
    }
}
