package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.adapters.GMapsPlannerAdapter;
import cz.cvut.fel.intermodal_planning.adapters.OTPlannerAdapter;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.GraphMaker;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.*;
import cz.cvut.fel.intermodal_planning.utils.LocationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import cz.cvut.fel.intermodal_planning.pathfinding.AStar;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class RoutePlanner {
    private static final Logger logger = LogManager.getLogger(RoutePlanner.class);

    private GraphMaker graphMaker;

    public RoutePlanner(GraphMaker graphMaker) {
        this.graphMaker = graphMaker;
    }


    public Route findRandomPath(LocationArea locationArea) {
        Location[] locArray = locationArea.generateRandomLocations(2);

        Route randomPath = null;
        while (randomPath.isEmpty()) {
            randomPath = findRoute(locArray[0], locArray[1]);
        }

        return randomPath;
    }

    private Route createRouteFromEdgeList(List<GraphEdge> edgeList, Location origin, Location destination) {
        Route route = new Route();

        route.origin = origin;
        route.destination = destination;

        if (edgeList.isEmpty()) return route;

        Leg tmpLeg = null;
        for (GraphEdge edge : edgeList) {
            if (tmpLeg == null) tmpLeg = new Leg();
            if (edge.transportMode != tmpLeg.transportMode) {
                if (tmpLeg.transportMode != null) route.legList.add(tmpLeg);
                tmpLeg = new Leg();
                tmpLeg.startLocation = LocationUtils.getNodeLocation(graphMaker.getGraph().getNode(edge.fromId));
                tmpLeg.transportMode = edge.transportMode;
            }

            Step step = new Step();
            step.startLocation = tmpLeg.startLocation;
            step.endLocation = tmpLeg.endLocation;
            step.transportMode = tmpLeg.transportMode;
            step.durationInSeconds = edge.durationInSeconds;

            tmpLeg.endLocation = LocationUtils.getNodeLocation(graphMaker.getGraph().getNode(edge.toId));
            tmpLeg.durationInSeconds += step.durationInSeconds;
            tmpLeg.steps.add(step);
        }
        return route;
    }

    public Route findRouteBySubplanner(Location locFrom, Location locTo, TransportMode mode) {
        Route route = null;

        if (mode == TransportMode.TRANSIT || mode == TransportMode.BICYCLE) {
            route = OTPlannerAdapter.getInstance().findRoute(locFrom, locTo, mode);
        } else if (mode == TransportMode.CAR || mode == TransportMode.WALK) {
            route = GMapsPlannerAdapter.getInstance().findRoute(locFrom, locTo, mode);
        }

        return route;
    }

    private Route findRouteBySubplanner(int fromId, int toId, TransportMode mode) {
        Graph graph = graphMaker.getGraph();

        Node from = graph.getNode(fromId);
        Node to = graph.getNode(toId);

        Location locFrom = LocationUtils.getNodeLocation(from);
        Location locTo = LocationUtils.getNodeLocation(to);

        return findRouteBySubplanner(locFrom, locTo, mode);

    }

    public Route findRoute(Location origin, Location destination) {
        return findRoute(origin, destination, new TransportMode[]{});
    }

    public Route findRoute(Location origin, Location destination, TransportMode... availableModes) {
        AStar astar = new AStar<>(graphMaker.getGraph());

        List<Node> originList;
        List<Node> destinationList;

        List<GraphEdge> astarPlan;

        if (availableModes.length == 0) {
            originList = getNearestNodes(origin, 5);
            destinationList = getNearestNodes(destination, 5);

            astarPlan = astar.plan(origin, destination, originList, destinationList);
        } else {
            originList = getNearestNodes(origin, availableModes, true, 1);
            destinationList = getNearestNodes(destination, availableModes, false, 1);

            astarPlan = astar.plan(origin, destination, originList, destinationList, availableModes);
        }

        return createRouteFromEdgeList(astarPlan, origin, destination);
    }

    public Route doRefinement(Route route) {
        Route refoundedRoute = new Route();
        refoundedRoute.origin = route.origin;
        refoundedRoute.destination = route.destination;

        for (Leg leg : route.legList) {
            Route tmpRoute = findRouteBySubplanner(leg.startLocation, leg.endLocation, leg.transportMode);
            refoundedRoute.legList.addAll(tmpRoute.legList);
        }

        return route;
    }

    private List<Node> getNearestNodes(Location location, int count) {
        Object[] nodeIdArr = graphMaker.getKdTree().nearest(location.toDoubleArray(), count);

        return Arrays.stream(nodeIdArr)
                .map(object -> (int) object)
                .map(nodeId -> graphMaker.getGraph().getNode(nodeId))
                .collect(Collectors.toList());
    }

    private List<Node> getNearestNodes(Location location, TransportMode[] modeArr, boolean isIngoingMode, int count) {
        int idx = ThreadLocalRandom.current().nextInt(modeArr.length);
        Object[] nodeIdArr = graphMaker.getKdTreeForMode(modeArr[idx], isIngoingMode).nearest(location.toDoubleArray(), count);

        return Arrays
                .stream(nodeIdArr)
                .map(nodeid -> graphMaker.getGraph().getNode((int) nodeid))
                .collect(Collectors.toList());
    }

    private Node getNearestNode(Location location) {
        return getNearestNodes(location, 1).get(0);
    }

    private Node getNearestNode(Location location, TransportMode[] mode, boolean isIngoingMode) {
        return getNearestNodes(location, mode, isIngoingMode, 1).get(0);
    }

    public long getRouteDuration(Route route) {
        if (route.isEmpty()) return 0;

        long duration = route.legList.stream().mapToLong(o -> o.durationInSeconds).sum();

        double originNodeDist = LocationUtils.distance(route.origin, route.legList.get(0).startLocation);
        duration += getDistanceDuration(route.legList.get(0).transportMode, originNodeDist);

        double destNodeDist = LocationUtils.distance(route.destination, route.legList.get(route.legList.size() - 1).endLocation);
        duration += getDistanceDuration(route.legList.get(route.legList.size() - 1).transportMode, destNodeDist);


        TransportMode prevMode = route.legList.get(0).transportMode;
        for (Leg leg : route.legList) {
            duration += (prevMode == leg.transportMode) ? 0 : getTransferPenalty(prevMode);
            prevMode = leg.transportMode;
        }

        if (route.legList.get(route.legList.size() - 1).transportMode == TransportMode.CAR) duration += 120;
        else if (route.legList.get(route.legList.size() - 1).transportMode == TransportMode.BICYCLE) duration += 60;

        return duration;
    }

    public static int getTransferPenalty(TransportMode prevMode) {
        if (prevMode == TransportMode.CAR) {
            return 120;
        } else if (prevMode == TransportMode.BICYCLE) {
            return 60;
        } else {
            return 0;
        }
    }

    public static boolean isTransferPossible(TransportMode prevMode, TransportMode currentMode) {
        if (prevMode == null || prevMode == currentMode) return true;
        if (currentMode == TransportMode.CAR) return false;
        if (currentMode == TransportMode.BICYCLE) return false;
        return true;
    }

    public static long getDistanceDuration(TransportMode mode, double distance) {
        switch (mode) {
            case CAR:
                return (long) (distance / Storage.CAR_SPEED_MPS);
            case TRANSIT:
                return (long) (distance / Storage.TRANSIT_SPEED_MPS);
            case BICYCLE:
                return (long) (distance / Storage.BIKE_SPEED_MPS);
            case WALK:
                return (long) (distance / Storage.WALK_SPEED_MPS);
            default:
                return 0;
        }
    }

    public Graph<Node, GraphEdge> getGraph() {
        return graphMaker.getGraph();
    }
}
