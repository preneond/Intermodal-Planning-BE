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


    public List<GraphEdge> findRandomPath(LocationArea locationArea) {
        Location[] locArray = locationArea.generateRandomLocations(2);

        return findPath(locArray[0], locArray[1]);
    }

    public Route findRouteBySubplanner(Location locFrom, Location locTo, TransportMode mode) {
        Route route = null;

        if (mode == TransportMode.TRANSIT || mode == TransportMode.BICYCLE) {
            route = OTPlannerAdapter.getInstance().findRoute(locFrom, locTo, mode);
        } else if (mode == TransportMode.CAR || mode == TransportMode.WALK) {
            route = GMapsPlannerAdapter.getInstance().findRoute(locFrom, locTo, mode);
        }

        if (route == null) route = new Route();

        return route;
    }

    private Route findRouteBySubplanner(int fromId, int toId, TransportMode mode) {
        Graph graph = graphMaker.getGraph();

        Node from = graph.getNode(fromId);
        Node to = graph.getNode(toId);

        Location locFrom = LocationUtils.getNodeLocation(from);
        Location locTo = LocationUtils.getNodeLocation(to);

        return findRouteBySubplanner(locFrom,locTo,mode);

    }

    public List<GraphEdge> findPath(Location origin, Location destination) {
        return findPath(origin, destination, new TransportMode[]{});
    }

    public List<GraphEdge> findPath(Location origin, Location destination, TransportMode... availableModes) {
        AStar astar = new AStar<>(graphMaker.getGraph());

        List<Node> originList;
        List<Node> destinationList;

        if (availableModes.length == 0) {
            originList = getNearestNodes(origin, 5);
            destinationList = getNearestNodes(destination, 5);

            return astar.plan(origin, destination, originList, destinationList);
        } else {
            originList = getNearestNodes(origin, availableModes, true, 5);
            destinationList = getNearestNodes(destination, availableModes, false, 5);

            return astar.plan(origin, destination, originList, destinationList, availableModes);
        }
    }

    public Route doRefinement(List<GraphEdge> plan) {
        if (plan.isEmpty()) return null;

        Graph graph = graphMaker.getGraph();

        GraphEdge startEdge = plan.get(0);
        int fromId = startEdge.fromId;
        int toId = startEdge.toId;
        GraphEdge curEdge;

        Route route = new Route();
        route.origin = LocationUtils.getNodeLocation(graph.getNode(fromId));
        route.destination = LocationUtils.getNodeLocation(graph.getNode(plan.get(plan.size() - 1).toId));

        for (int i = 1; i < plan.size(); i++) {
            curEdge = plan.get(i);
            if (startEdge.mode != curEdge.mode) {
                Route tmpRoute = findRouteBySubplanner(fromId, toId, startEdge.mode);
                route.legList.addAll(tmpRoute.legList);
                startEdge = curEdge;
                fromId = startEdge.fromId;
                toId = startEdge.toId;
            } else {
                toId = curEdge.toId;
            }
        }
        Route tmpRoute = findRouteBySubplanner(fromId, toId, startEdge.mode);
        route.legList.addAll(tmpRoute.legList);

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

    public long getDuration(List<GraphEdge> graphPath) {
        if (graphPath.isEmpty()) return 0;
        long duration = graphPath.stream().mapToLong(o -> o.durationInSeconds).sum();
        TransportMode prevMode = graphPath.get(0).mode;
        for (GraphEdge edge : graphPath) {
            // penalty for transfer
            duration += (prevMode == edge.mode) ? 0 : getTransferPenalty(prevMode);
            prevMode = edge.mode;
        }
        if (graphPath.get(graphPath.size() - 1).mode == TransportMode.CAR) duration += 120;
        if (graphPath.get(graphPath.size() - 1).mode == TransportMode.BICYCLE) duration += 60;

        return duration;
    }

    public long getDuration(Route route) {
        return route == null ? 0 : route.legList.stream().mapToLong(o -> o.durationInSeconds).sum();
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
