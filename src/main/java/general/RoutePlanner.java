package general;

import adapters.GMapsPlannerAdapter;
import adapters.OpenTripPlannerAdapter;
import adapters.PlannerAdapter;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.*;
import pathfinding.AStar;
import utils.RandomLocationGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RoutePlanner {
    private PlannerAdapter[] plannerAdapters;

    private static final short NUM_OF_PATHS = 5;

    RoutePlanner() {
        // add more planner adapters if they exist
        plannerAdapters = new PlannerAdapter[]{new GMapsPlannerAdapter(), new OpenTripPlannerAdapter()};
    }

    public List<Route> expandGraph() {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (PlannerAdapter plannerAdapter : plannerAdapters) {
            // Uncomment for loop for generating more routes
            for (int i = 0; i < NUM_OF_PATHS; i++) {
                locArray = RandomLocationGenerator.getInstance().generateLocationsInPrague(2);
                routeList = plannerAdapter.findRoutes(locArray[0], locArray[1]);
                routes.addAll(routeList);
                routeList = plannerAdapter.findRoutes(locArray[1], locArray[0]);
                routes.addAll(routeList);
            }
        }

        return routes;
    }

    public List<GraphEdge> findRandomPath() {
        Graph graph = GraphMaker.getInstance().getGraph();

        int numOfNodes = graph.getAllNodes().size();
        Node origin = graph.getNode(ThreadLocalRandom.current().nextInt(numOfNodes));
        Node destination = graph.getNode(ThreadLocalRandom.current().nextInt(numOfNodes));

        AStar astar = new AStar<>(graph);
        List<GraphEdge> plan = astar.plan(origin, destination);

        if (plan == null) return new ArrayList<>();

        return plan;
    }

    private Route findPath(int fromId, int toId, TransportMode mode) {
        Graph graph = GraphMaker.getInstance().getGraph();

        Node from = graph.getNode(fromId);
        Node to = graph.getNode(toId);

        Route route = null;

        Location locFrom = new Location(from.getLatitude(), from.getLongitude());
        Location locTo = new Location(to.getLatitude(), to.getLongitude());
        if (mode == TransportMode.TRANSIT) {
            route = new OpenTripPlannerAdapter().findRoute(locFrom, locTo, TransportMode.TRANSIT);
        } else if (mode == TransportMode.CAR || mode == TransportMode.WALK) {
            route = new GMapsPlannerAdapter().findRoute(locFrom, locTo, mode);
        }

        return route;
    }

    public List<Location> getLocationsFromLegs(List<Leg> legList) {
        List<Location> locationList = new ArrayList<>();
        for (Leg leg : legList) {
            for (Step step : leg.steps) {
                locationList.add(step.startLocation);
            }
        }
        return locationList;
    }


    public List<Location> getLocationsFromEdges(List<GraphEdge> edgeList) {
        if (edgeList.isEmpty()) return new ArrayList<>();

        Graph graph = GraphMaker.getInstance().getGraph();
        List<Location> locationList = new ArrayList<>();
        Node nodeFrom;
        for (GraphEdge edge : edgeList) {
            nodeFrom = graph.getNode(edge.fromId);
            locationList.add(new Location(nodeFrom.getLatitude(), nodeFrom.getLongitude()));
        }
        Node nodeTo = graph.getNode(edgeList.get(edgeList.size() - 1).toId);
        locationList.add(new Location(nodeTo.getLatitude(), nodeTo.getLongitude()));

        return locationList;
    }


    public List<Location> doRefinement(List<GraphEdge> plan) {
        if (plan.isEmpty()) return null;

        GraphEdge startEdge = plan.get(0);
        int fromId = startEdge.fromId;
        int toId = startEdge.toId;
        GraphEdge curEdge;

        Route resultingRoute = new Route();
        for (int i = 1; i < plan.size(); i++) {
            curEdge = plan.get(i);
            if (startEdge.mode != curEdge.mode) {
                Route route = findPath(fromId, toId, startEdge.mode);
                resultingRoute.legList.addAll(route.legList);
                startEdge = curEdge;
                fromId = startEdge.fromId;
                toId = startEdge.toId;
            } else {
                toId = curEdge.toId;
            }
        }
        return getLocationsFromLegs(resultingRoute.legList);
    }
}
