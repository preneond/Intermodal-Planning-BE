package general;

import adapters.GMapsPlannerAdapter;
import adapters.OpenTripPlannerAdapter;
import adapters.PlannerAdapter;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import pathfinding.AStar;

import java.util.ArrayList;
import java.util.List;

public class RoutePlanner {
    private static final Logger logger = LogManager.getLogger(RoutePlanner.class);
    private GraphMaker graphMaker;
    private PlannerAdapter[] plannerAdapters;

    public RoutePlanner(GraphMaker graphMaker) {
        // add more planner adapters if they exist
        this.graphMaker = graphMaker;
        plannerAdapters = new PlannerAdapter[]{GMapsPlannerAdapter.getInstance(), OpenTripPlannerAdapter.getInstance()};
    }

    public List<Route> getRoutesFromKnownRequest(int numOfRequests) {
        List<Route> resultRoutes = new ArrayList<>();
        List<Route> tmpResult;
        for (int i = 1; i <= numOfRequests; i++) {
            for (TransportMode mode : TransportMode.availableModes()) {
                tmpResult = null;
                if (mode == TransportMode.TRANSIT || mode == TransportMode.BICYCLE) {
                    tmpResult = OpenTripPlannerAdapter.getInstance().findRoutesFromKnownRequests(i, mode);
                } else if (mode == TransportMode.CAR || mode == TransportMode.WALK) {
                    tmpResult = GMapsPlannerAdapter.getInstance().findRoutesFromKnownRequests(i, mode);
                }
                if (tmpResult == null) continue;

                resultRoutes.addAll(tmpResult);
            }
        }
        return resultRoutes;
    }

    public void expandGraphFromKnownRequests(int numOfrequests) {
        graphMaker.createGraph(getRoutesFromKnownRequest(numOfrequests));
    }

    public List<Route> expandGraph(int numOfRequests, PlannerAdapter plannerAdapter) {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (int i = 0; i < numOfRequests; i++) {
            locArray = Location.generateRandomLocationsInPrague(2);
            routeList = plannerAdapter.findRoutes(locArray[0], locArray[1]);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locArray[1], locArray[0]);
            routes.addAll(routeList);
        }

        return routes;
    }

    public List<Route> expandGraph(int numOfRequests, TransportMode transportMode) {
        switch (transportMode) {
            case BICYCLE:
                return expandGraph(numOfRequests, OpenTripPlannerAdapter.getInstance(), transportMode);
            case TRANSIT:
                return expandGraph(numOfRequests, OpenTripPlannerAdapter.getInstance());
            default:
                return expandGraph(numOfRequests, GMapsPlannerAdapter.getInstance(), transportMode);
        }
    }

    private List<Route> expandGraph(int numOfRequests, PlannerAdapter plannerAdapter, TransportMode transportMode) {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (int i = 0; i < numOfRequests; i++) {
            locArray = Location.generateRandomLocationsInPrague(2);
            routeList = plannerAdapter.findRoutes(locArray[0], locArray[1],transportMode);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locArray[1], locArray[0],transportMode);
            routes.addAll(routeList);
        }

        return routes;
    }

    public void expandGraph(int numOfRequests) {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (PlannerAdapter plannerAdapter : plannerAdapters) {
            // Uncomment for loop for generating more routes
            routeList = expandGraph(numOfRequests, plannerAdapter);
            routes.addAll(routeList);
        }

        graphMaker.createGraph(routes);
    }

    public List<GraphEdge> findRandomPath() {
        Location[] locArray = Location.generateRandomLocationsInPrague(2);

        return findPath(locArray[0], locArray[1]);
    }

    private Route findRoute(int fromId, int toId, TransportMode mode) {
        Graph graph = graphMaker.getGraph();

        Node from = graph.getNode(fromId);
        Node to = graph.getNode(toId);

        Route route = null;

        Location locFrom = new Location(from.getLatitude(), from.getLongitude());
        Location locTo = new Location(to.getLatitude(), to.getLongitude());
        if (mode == TransportMode.TRANSIT) {
            route = OpenTripPlannerAdapter.getInstance().findRoute(locFrom, locTo, TransportMode.TRANSIT);
        } else if (mode == TransportMode.CAR || mode == TransportMode.WALK) {
            route = GMapsPlannerAdapter.getInstance().findRoute(locFrom, locTo, mode);
        }

        if (route == null) return new Route();

        return route;
    }

    public List<Location> getLocationSequence(Route route) {
        List<Location> locationList = new ArrayList<>();
        locationList.add(route.origin);

        for (Leg leg : route.legList) {
            for (Step step : leg.steps) {
                locationList.add(step.startLocation);
                locationList.add(step.endLocation);
            }

        }
        locationList.add(route.destination);
        return locationList;
    }


    public List<Location> getLocationsFromEdges(List<GraphEdge> edgeList, Graph<Node, GraphEdge> graph) {
        if (edgeList.isEmpty()) return new ArrayList<>();

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


    public Route doRefinement(List<GraphEdge> plan) {
        if (plan.isEmpty()) return null;

        Graph graph = graphMaker.getGraph();

        GraphEdge startEdge = plan.get(0);
        int fromId = startEdge.fromId;
        int toId = startEdge.toId;
        GraphEdge curEdge;

        Route route = new Route();
        route.origin = Location.getLocation(graph.getNode(fromId));
        route.destination = Location.getLocation(graph.getNode(plan.get(plan.size() - 1).toId));

        for (int i = 1; i < plan.size(); i++) {
            curEdge = plan.get(i);
            if (startEdge.mode != curEdge.mode) {
                Route tmpRoute = findRoute(fromId, toId, startEdge.mode);
                route.legList.addAll(tmpRoute.legList);
                startEdge = curEdge;
                fromId = startEdge.fromId;
                toId = startEdge.toId;
            } else {
                toId = curEdge.toId;
            }
        }
        Route tmpRoute = findRoute(fromId, toId, startEdge.mode);
        route.legList.addAll(tmpRoute.legList);

        return route;
    }

    public void getPathDescription(Route route, String name) {
        if (route.legList == null || route.legList.isEmpty()) {
            logger.error("Path is empty");
            return;
        }

        long routeDuration = route.legList.stream().mapToLong(o -> o.durationInSeconds).sum();
        logger.info("Duration of " + name + " path: " + routeDuration + " seconds");
        logger.info("Number of transfers: " + route.legList.size());
    }

    public void getPathDescription(List<GraphEdge> path, String name) {
        if (path == null || path.isEmpty()) {
            logger.error("Path is empty");
            return;
        }

        int numOfTransfers = 0;

        GraphEdge curEdge;
        GraphEdge prevEdge;
        for (int i = 1; i < path.size(); i++) {
            prevEdge = path.get(i - 1);
            curEdge = path.get(i);
            numOfTransfers += (prevEdge.mode == curEdge.mode) ? 0 : 1;
        }

        long routeDuration = path.stream().mapToLong(o -> o.durationInSeconds).sum();
        logger.info("Duration of " + name + " path: " + routeDuration + " seconds");
        logger.info("Number of transfers: " + numOfTransfers);

    }

    public List<GraphEdge> findPath(Location origin, Location destination) {
        return findPath(origin, destination, TransportMode.availableModes());
    }

    public List<GraphEdge> findPath(Location origin, Location destination, TransportMode... availableModes) {
        Node originNode;
        Node destinationNode;
        if (availableModes == null) {
            originNode = getNearestNode(origin);
            destinationNode = getNearestNode(destination);
        } else {
            originNode = getNearestNode(origin, availableModes[0], true);
            destinationNode = getNearestNode(destination, availableModes[0], false);
        }
        AStar astar = new AStar<>(graphMaker.getGraph());
        List<GraphEdge> plan = astar.plan(originNode, destinationNode, availableModes);

//        if (plan == null) logger.debug("Plan is empty");
//            return findRandomPath();
//        }

//        logger.info("Generated random origin location" + origin);
//        logger.info("Generated random destination location" + destination);
//        logger.info("Founded node as origin location" + Location.getLocation(originNode));
//        logger.info("Founded node as destination location" + Location.getLocation(destinationNode));

        return plan;


    }

    private Node getNearestNode(Location location) {
        int nodeId = (int) graphMaker.getKdTree().nearest(location.toDoubleArray());

        return graphMaker.getGraph().getNode(nodeId);
    }

    private Node getNearestNode(Location location, TransportMode mode, boolean isIngoingMode) {
        int nodeId = (int) graphMaker.getKdTreeForMode(mode, isIngoingMode).nearest(location.toDoubleArray());

        return graphMaker.getGraph().getNode(nodeId);
    }

    public long getDuration(List<GraphEdge> graphPath) {
        return graphPath.stream().mapToLong(o -> o.durationInSeconds).sum();
    }

    public long getDuration(Route route) {
        return route == null ? 0 : route.legList.stream().mapToLong(o -> o.durationInSeconds).sum();
    }
}
