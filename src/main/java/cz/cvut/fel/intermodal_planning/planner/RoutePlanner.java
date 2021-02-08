package cz.cvut.fel.intermodal_planning.planner;

import cz.cvut.fel.intermodal_planning.planner.model.*;
import cz.cvut.fel.intermodal_planning.subplanners.adapters.GMapsPlannerAdapter;
import cz.cvut.fel.intermodal_planning.subplanners.adapters.OTPlannerAdapter;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.GraphMaker;
import cz.cvut.fel.intermodal_planning.graph.model.GraphEdge;
import cz.cvut.fel.intermodal_planning.general.utils.LocationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import cz.cvut.fel.intermodal_planning.planner.pathfinding.ShortestPathAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class RoutePlanner {
    private static final Logger logger = LogManager.getLogger(RoutePlanner.class);

    private GraphMaker graphMaker;

    public RoutePlanner(GraphMaker graphMaker) {
        this.graphMaker = graphMaker;
    }


    /**
     * Creation of Route from Graph's Edge sequence
     * @param edgeList Edge sequence
     * @param origin Origin Location
     * @param destination Destination Location
     * @return Created Route
     */
    private Route createRouteFromEdgeList(List<GraphEdge> edgeList, Location origin, Location destination) {
        Route route = new Route();

        route.origin = origin;
        route.destination = destination;

        if (edgeList == null) return route;

        Leg tmpLeg = null;
        GraphEdge edge;
        for (int i = 0; i < edgeList.size(); i++) {
            edge = edgeList.get(i);

            Step step = new Step();
            step.startLocation = LocationUtils.getNodeLocation(graphMaker.getGraph().getNode(edge.fromId));
            step.endLocation = LocationUtils.getNodeLocation(graphMaker.getGraph().getNode(edge.toId));
            step.transportMode = edge.transportMode;
            step.durationInSeconds = edge.durationInSeconds;

            if (i == 0) {
                tmpLeg = new Leg();
                tmpLeg.startLocation = step.startLocation;
                tmpLeg.durationInSeconds = step.durationInSeconds;
                tmpLeg.transportMode = step.transportMode;
            } else if (edge.transportMode != tmpLeg.transportMode) {
                route.legList.add(tmpLeg);
                tmpLeg = new Leg();
                tmpLeg.startLocation = step.startLocation;
                tmpLeg.durationInSeconds += edge.durationInSeconds;
                tmpLeg.transportMode = edge.transportMode;
            } else {
                tmpLeg.durationInSeconds += edge.durationInSeconds;
            }
            tmpLeg.endLocation = step.endLocation;
            tmpLeg.steps.add(step);

            if (i == edgeList.size()-1) route.legList.add(tmpLeg);
        }

        return route;
    }

    public Route searchRouteUsingSubplanner(Location locFrom, Location locTo, TransportMode mode) {
        Route route = null;

        if (mode == TransportMode.TRANSIT || mode == TransportMode.BICYCLE) {
            route = OTPlannerAdapter.getInstance().findRoute(locFrom, locTo, mode);
        } else if (mode == TransportMode.CAR || mode == TransportMode.WALK) {
            route = GMapsPlannerAdapter.getInstance().findRoute(locFrom, locTo, mode);
        }

        return route;
    }

    private Route searchRouteUsingSubplanner(int fromId, int toId, TransportMode mode) {
        Graph graph = graphMaker.getGraph();

        Node from = graph.getNode(fromId);
        Node to = graph.getNode(toId);

        Location locFrom = LocationUtils.getNodeLocation(from);
        Location locTo = LocationUtils.getNodeLocation(to);

        return searchRouteUsingSubplanner(locFrom, locTo, mode);

    }

    /**
     * Route metasearch
     *
     * @param origin Origin Location
     * @param destination Destination Location
     * @return metasearched Route
     */
    public Route metasearchRoute(Location origin, Location destination) {
        return metasearchRoute(origin, destination, new TransportMode[]{});
    }

    /**
     *  Route Metasearch
     *
     * @param nodeFrom Origin Node
     * @param nodeTo Destination Node
     * @param availableModes Possible Transport Modes to use
     * @return metasearched Route
     */
    public Route metasearchRoute(Node nodeFrom, Node nodeTo, TransportMode... availableModes) {
        ShortestPathAlgorithm astar = new ShortestPathAlgorithm<>(graphMaker.getGraph());

        List<Node> originList = new ArrayList<>();
        List<Node> destinationList = new ArrayList<>();
        List<GraphEdge> astarPlan;


        Location locFrom = LocationUtils.getNodeLocation(nodeFrom);
        Location locTo = LocationUtils.getNodeLocation(nodeTo);

        originList.add(nodeFrom);
        destinationList.add(nodeTo);

        astarPlan = astar.plan(locFrom, locTo, originList, destinationList, availableModes);

        return createRouteFromEdgeList(astarPlan, locFrom, locTo);
    }

    /**
     * Route metasearch
     *
     * @param origin Origin Location
     * @param destination Destination Location
     * @param availableModes Allowed Transport Modes to use
     *
     * @return Metasearched Route
     */
    public Route metasearchRoute(Location origin, Location destination, TransportMode... availableModes) {
        ShortestPathAlgorithm astar = new ShortestPathAlgorithm<>(graphMaker.getGraph());

        List<Node> originList;
        List<Node> destinationList;

        List<GraphEdge> astarPlan;

        if (availableModes.length == 0) {
            originList = getNearestNodes(origin, 5);
            destinationList = getNearestNodes(destination, 5);

            astarPlan = astar.plan(origin, destination, originList, destinationList);
        } else {
            originList = getNearestNodes(origin, availableModes, true, 5);
            destinationList = getNearestNodes(destination, availableModes, false, 5);

            astarPlan = astar.plan(origin, destination, originList, destinationList, availableModes);
        }

        return createRouteFromEdgeList(astarPlan, origin, destination);
    }

    /**
     * Random route metasearch
     *
     * @param locationArea Selected Test Region
     * @return metasearched Route
     */
    public Route metasearchRandomRoute(LocationArea locationArea) {
        Location[] locArr;
        Route route = new Route();
        while (route.isEmpty()) {
            locArr = locationArea.generateRandomLocations(2);
            route = metasearchRoute(locArr[0], locArr[1]);
        }
        return route;
    }

    /**
     * Route between randomly selected Graph's nodes
     *
     * @return metasearched Route
     */
    public Route metaSearchRouteBetweenRandomNodes() {
        List<Node> nodeList = (List<Node>) graphMaker.getGraph().getAllNodes();
        Node from = nodeList.get(ThreadLocalRandom.current().nextInt(nodeList.size()));
        Node to = nodeList.get(ThreadLocalRandom.current().nextInt(nodeList.size()));

        Route randomPath = new Route();
        while (randomPath.isEmpty()) {
            randomPath = metasearchRoute(from, to, new TransportMode[]{});
        }

        return randomPath;
    }

    /**
     * Route Refinement
     * @param route Route to refine
     * @return refined Route
     */
    public Route doRefinement(Route route) {
        Route refoundedRoute = new Route();
        refoundedRoute.origin = route.origin;
        refoundedRoute.destination = route.destination;
        for (Leg leg : route.legList) {
            Route tmpRoute = searchRouteUsingSubplanner(leg.startLocation, leg.endLocation, leg.transportMode);
            if (tmpRoute == null) refoundedRoute.legList.add(leg);
            else refoundedRoute.legList.addAll(tmpRoute.legList);
        }

        return refoundedRoute;
    }

    /**
     * Nearest nodes for given location
     * @param location Location for which nearest nodes are searched
     * @param count number of Nodes, which is returned
     * @return  List of Nearest Nodes
     */
    private List<Node> getNearestNodes(Location location, int count) {
        Object[] nodeIdArr = graphMaker.getKdTree().nearest(location.toDoubleArray(), count);

        return Arrays.stream(nodeIdArr)
                .map(object -> (int) object)
                .map(nodeId -> graphMaker.getGraph().getNode(nodeId))
                .collect(Collectors.toList());
    }

    /**
     * Nearest nodes for given location where the given transport modes are possible to use
     *
     * @param location Location for which nearest nodes are searched
     * @param modeArr Transport Mode Array which are a
     * @param isIngoingMode check whether it should be looked for ingoing modes or outgoing modes
     * @param count number of Nodes, which is returned
     * @return List of Nearest Nodes
     */
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

    /**
     * Route Duration with all transfer penalties
     *
     * @param route Route object
     * @return Route Duration in Seconds
     */
    public long getRouteDuration(Route route) {
        long duration = route.legList.stream().mapToLong(o -> o.durationInSeconds).sum();

        double originNodeDist = LocationUtils.distance(route.origin, route.legList.get(0).startLocation);
        duration += getDistanceDuration(TransportMode.WALK, originNodeDist);

        double destNodeDist = LocationUtils.distance(route.destination, route.legList.get(route.legList.size() - 1).endLocation);
        duration += getDistanceDuration(TransportMode.WALK, destNodeDist);


        TransportMode prevMode = route.legList.get(0).transportMode;
        for (Leg leg : route.legList) {
            duration += (prevMode == leg.transportMode) ? 0 : getTransferPenalty(prevMode);
            prevMode = leg.transportMode;
        }

        if (route.legList.get(route.legList.size() - 1).transportMode == TransportMode.CAR) duration += 120;
        else if (route.legList.get(route.legList.size() - 1).transportMode == TransportMode.BICYCLE) duration += 60;

        return duration;
    }

    /**
     * Method is called when the previous and current method differ
     *
     * @param prevMode Previous mode
     * @return Penalty in Seconds
     */
    public static int getTransferPenalty(TransportMode prevMode) {
        if (prevMode == TransportMode.CAR) {
            return 120;
        } else if (prevMode == TransportMode.BICYCLE) {
            return 60;
        } else {
            return 0;
        }
    }

    /**
     * Check whether the transfer is possible ->
     * @param prevMode
     * @param currentMode
     * @return
     */
    public static boolean isTransferPossible(TransportMode prevMode, TransportMode currentMode) {
        if (prevMode == null || prevMode == currentMode) return true;
        if (currentMode == TransportMode.CAR) return false;
        if (currentMode == TransportMode.BICYCLE) return false;
        return true;
    }

    /**
     * Distance duration for given transport mode
     *
     * @param mode Transport Mode
     * @param distance Distance in Meters
     * @return Duration in Seconds
     */
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

    /**
     * Graph Getter
     * @return Graph object
     */
    public Graph<Node, GraphEdge> getGraph() {
        return graphMaker.getGraph();
    }
}
