import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class SpaceportTransitPlanner {
    Map<Station, Station> prev = new HashMap<>();
    Map<String, Double> cost = new HashMap<>();

    private final Map<Station, Boolean> previousEdgeIsShuttle = new HashMap<>();
    private final Map<Station, Double> previousEdgeCost = new HashMap<>();
    private final Map<Edge, Boolean> isShuttleEdge = new IdentityHashMap<>();

    double dist(Station first, Station second) {
        double dx = first.p.x - second.p.x;
        double dy = first.p.y - second.p.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void rememberCost(Station from, Station to, double minutes) {
        cost.put(from.name + to.name, minutes);
        cost.put(from.name + "->" + to.name, minutes);
        cost.put(from.name + " " + to.name, minutes);
    }

    void addUndirected(Station first, Station second, double speed) {
        double minutes = dist(first, second) / speed;
        Edge forward = new Edge(second, minutes);
        Edge reverse = new Edge(first, minutes);

        first.edges.add(forward);
        second.edges.add(reverse);
        isShuttleEdge.put(forward, false);
        isShuttleEdge.put(reverse, false);
        rememberCost(first, second, minutes);
        rememberCost(second, first, minutes);
    }

    void addDirected(Station from, Station to, double speed) {
        double minutes = dist(from, to) / speed;
        Edge edge = new Edge(to, minutes);
        from.edges.add(edge);
        isShuttleEdge.put(edge, true);
        rememberCost(from, to, minutes);
    }

    public List<RouteInstruction> solve(SpaceportTransitNetwork net) {
        RouteContext context = RouteContext.from(net);
        if (!context.ready()) return new ArrayList<>();

        List<Station> stations = collectStations(context);
        rebuildGraph(stations, context.network);
        SearchResult result = runDijkstra(context.start, context.end, stations);
        if (!result.found() && context.start != context.end) return new ArrayList<>();
        return traceRoute(context.start, context.end);
    }

    private List<Station> collectStations(RouteContext context) {
        List<Station> stations = new ArrayList<>();
        addStation(stations, context.start);

        if (context.network.corridors != null) {
            for (ShuttleCorridor corridor : context.network.corridors) {
                if (corridor == null || corridor.stations == null) continue;
                for (Station station : corridor.stations) addStation(stations, station);
            }
        }

        addStation(stations, context.end);
        return identityUnique(stations);
    }

    private void addStation(List<Station> stations, Station station) {
        if (station != null) stations.add(station);
    }

    private List<Station> identityUnique(List<Station> source) {
        List<Station> result = new ArrayList<>();
        Set<Station> seen = Collections.newSetFromMap(new IdentityHashMap<Station, Boolean>());
        for (Station station : source) {
            if (seen.add(station)) result.add(station);
        }
        return result;
    }

    private void rebuildGraph(List<Station> stations, SpaceportTransitNetwork network) {
        clearSearchState();
        for (Station station : stations) {
            if (station.edges == null) station.edges = new ArrayList<>();
            station.edges.clear();
        }

        for (int i = 0; i < stations.size(); i++) {
            for (int j = i + 1; j < stations.size(); j++) {
                addUndirected(stations.get(i), stations.get(j), network.walkSpeed);
            }
        }

        if (network.corridors == null) return;
        for (ShuttleCorridor corridor : network.corridors) {
            if (corridor == null || corridor.stations == null) continue;
            for (int i = 0; i + 1 < corridor.stations.size(); i++) {
                addDirected(corridor.stations.get(i), corridor.stations.get(i + 1), network.shuttleSpeed);
            }
        }
    }

    private void clearSearchState() {
        prev.clear();
        cost.clear();
        previousEdgeIsShuttle.clear();
        previousEdgeCost.clear();
        isShuttleEdge.clear();
    }

    private SearchResult runDijkstra(Station start, Station end, List<Station> stations) {
        Map<Station, Double> best = new IdentityHashMap<>();
        for (Station station : stations) best.put(station, Double.POSITIVE_INFINITY);
        best.put(start, 0.0);

        PriorityQueue<State> queue = new PriorityQueue<>();
        queue.add(new State(start, 0.0));

        while (!queue.isEmpty()) {
            State state = queue.poll();
            Station current = state.station;
            if (state.distance > best.get(current) + 1e-9) continue;
            if (current == end) return new SearchResult(true);

            for (Edge edge : current.edges) {
                relax(queue, best, current, edge);
            }
        }

        return new SearchResult(start == end || prev.containsKey(end));
    }

    private void relax(PriorityQueue<State> queue, Map<Station, Double> best, Station current, Edge edge) {
        Station next = edge.to;
        double candidateDistance = best.get(current) + edge.w;
        double knownDistance = best.containsKey(next) ? best.get(next) : Double.POSITIVE_INFINITY;
        boolean shuttleEdge = Boolean.TRUE.equals(isShuttleEdge.get(edge));

        if (candidateDistance + 1e-9 < knownDistance
                || shouldUseTie(current, next, candidateDistance, knownDistance, shuttleEdge)) {
            best.put(next, candidateDistance);
            prev.put(next, current);
            previousEdgeIsShuttle.put(next, shuttleEdge);
            previousEdgeCost.put(next, edge.w);
            queue.add(new State(next, candidateDistance));
        }
    }

    private List<RouteInstruction> traceRoute(Station start, Station end) {
        LinkedList<RouteInstruction> route = new LinkedList<>();
        Station current = end;

        while (current != start) {
            Station previous = prev.get(current);
            if (previous == null) return new ArrayList<>();
            route.addFirst(new RouteInstruction(
                    previous.name,
                    current.name,
                    previousEdgeCost.get(current),
                    Boolean.TRUE.equals(previousEdgeIsShuttle.get(current))));
            current = previous;
        }

        return new ArrayList<>(route);
    }

    private boolean shouldUseTie(Station candidatePrev, Station next, double candidate, double old, boolean candidateShuttle) {
        if (Math.abs(candidate - old) > 1e-9) return false;

        Station oldPrev = prev.get(next);
        if (oldPrev == null) return true;

        boolean oldShuttle = Boolean.TRUE.equals(previousEdgeIsShuttle.get(next));
        if (candidateShuttle != oldShuttle) return candidateShuttle;

        return candidatePrev.name.compareTo(oldPrev.name) < 0;
    }

    public List<RouteInstruction> getFastestRouteInstructions(SpaceportTransitNetwork net) {
        return solve(net);
    }

    public void print(List<RouteInstruction> route) {
        double total = 0;
        for (RouteInstruction instruction : route) total += instruction.t;

        System.out.println("Fastest route takes " + Math.round(total) + " minute(s).");
        System.out.println("Route Instructions");
        System.out.println("------------------");

        for (int i = 0; i < route.size(); i++) {
            RouteInstruction instruction = route.get(i);
            String action = instruction.shuttle ? "Take the shuttle" : "Walk";
            System.out.printf(Locale.US, "%d. %s from \"%s\" to \"%s\" for %.2f minutes.\n",
                    i + 1, action, instruction.a, instruction.b, instruction.t);
        }
    }

    public void printRouteInstructions(List<RouteInstruction> route) {
        print(route);
    }

    private static class State implements Comparable<State> {
        Station station;
        double distance;

        State(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }

        @Override
        public int compareTo(State other) {
            int byDistance = Double.compare(distance, other.distance);
            if (byDistance != 0) return byDistance;
            return station.name.compareTo(other.station.name);
        }
    }

    private static class RouteContext {
        SpaceportTransitNetwork network;
        Station start;
        Station end;

        static RouteContext from(SpaceportTransitNetwork network) {
            RouteContext context = new RouteContext();
            context.network = network;
            if (network != null) {
                context.start = network.start != null ? network.start : network.origin;
                context.end = network.end != null ? network.end : network.destination;
            }
            return context;
        }

        boolean ready() {
            return network != null && start != null && end != null;
        }
    }

    private static class SearchResult {
        private final boolean reachable;

        SearchResult(boolean reachable) {
            this.reachable = reachable;
        }

        boolean found() {
            return reachable;
        }
    }
}
