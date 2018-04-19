package cz.cvut.fel.intermodal_planning.graph;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.GraphBuilder;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.adapters.GMapsPlannerAdapter;
import cz.cvut.fel.intermodal_planning.adapters.OTPlannerAdapter;
import cz.cvut.fel.intermodal_planning.adapters.PlannerAdapter;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphExpansionStrategy;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.*;
import cz.cvut.fel.intermodal_planning.pathfinding.kdtree.KDTree;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Ondrej Prenek on 27/07/2017.
 * This code is owned by Umotional s.r.o. (IN: 03974618).
 * All Rights Reserved.
 */
public class GraphMaker extends GraphBuilder {
    private Graph<Node, GraphEdge> graph;
    private KDTree kdTree;

    private Map<TransportMode, KDTree> ingoingKDTreeMap;
    private Map<TransportMode, KDTree> outgoingKDTreeMap;
    private Map<Integer, Set<TransportMode>> nodeIncomingModes = new HashMap<>();
    private Map<Integer, Set<TransportMode>> nodeOutcomingModes = new HashMap<>();
    private int nodeCounter = 0;

    private static final Logger logger = LogManager.getLogger(GraphMaker.class);

    public Graph<Node, GraphEdge> getGraph() {
        return graph;
    }

    public Graph<Node, GraphEdge> createGraph(List<Route> routeList) {
        addRoutes(routeList);

        graph = createGraph();
        nodeCounter = 0;

        return graph;
    }


    public void setGraph(Graph<Node, GraphEdge> graph) {
        addNodes(graph.getAllNodes());
        addEdgeCollection(graph.getAllEdges());
        this.graph = createGraph();
        nodeCounter = 0;
    }

    private void addRoutes(List<Route> routes) {
        for (Route route : routes) {
            addLegs(route.legList);
        }
    }

    private void addLegs(List<Leg> legs) {
        for (Leg leg : legs) {
            if (leg.steps.isEmpty()) {
                int startId = getIdFor(leg.startLocation);
                int endId = getIdFor(leg.endLocation);
                if (!containsEdge(startId, endId)) {
                    GraphEdge edge = new GraphEdge(startId, endId, (int) leg.durationInSeconds);
                    edge.mode = leg.transportMode;
                    edge.durationInSeconds = leg.durationInSeconds;
                    addEdge(edge);
                }
            } else addSteps(leg.steps);
        }
    }

    private void addSteps(List<Step> steps) {
        for (Step step : steps) {
            if (step.substeps != null) {
                this.addSteps(step.substeps);
                continue;
            }
            int startId = getIdFor(step.startLocation);
            int endId = getIdFor(step.endLocation);
            if (!containsEdge(startId, endId)) {
                GraphEdge edge = new GraphEdge(startId, endId, (int) step.durationInSeconds);
                edge.mode = step.transportMode;
                edge.durationInSeconds = step.durationInSeconds;
                addEdge(edge);
            }
        }
    }

    private void addEdgeCollection(Collection<GraphEdge> edges) {
        for (GraphEdge edge : edges) {
            if (!containsEdge(edge.fromId, edge.toId)) {
                addEdge(edge);
            }
        }
    }

    private void addEdge(GraphEdge edge) {
        super.addEdge(edge);
        int fromId = edge.fromId;
        int toId = edge.toId;
        TransportMode edgeMode = edge.mode;

        Set<TransportMode> set = nodeIncomingModes.get(fromId);
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(edgeMode);
        nodeIncomingModes.put(fromId, set);

        set = nodeOutcomingModes.get(toId);
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(edgeMode);
        nodeOutcomingModes.put(toId, set);
    }


    /**
     * Method which return id for current location and create node whether ain't exists.
     *
     * @param location - location which is unique for each node
     * @return id for node on given location
     */
    private int getIdFor(Location location) {
        int id;
        Node node;
        try {
            id = getIntIdForSourceId(generateSourceIdFor(location));
        } catch (NullPointerException e) {
            id = nodeCounter;
            node = new Node(nodeCounter, generateSourceIdFor(location), location.lat, location.lon,
                    location.latE3(), location.lonE3(), 0);
            addNode(node);
            nodeCounter++;
        }

        return id;
    }

    /**
     * Create unique Node source id based on location using Szudzik's function
     * <p>
     * See https://stackoverflow.com/questions/919612/mapping-two-integers-to-one-in-a-unique-and-deterministic-way
     *
     * @param location - location which is unique for each node
     * @return unique sourceId for given location
     */
    public static int generateSourceIdFor(Location location) {
        int a = location.latE3();
        int b = location.lonE3();
        int A = a >= 0 ? 2 * a : -2 * a - 1;
        int B = b >= 0 ? 2 * b : -2 * b - 1;

        return A >= B ? A * A + A + B : A + B * B;
    }

    public void createKDTree() {
        logger.info("Creating KDTree...");
        double[] tmpArr = new double[2];
        kdTree = new KDTree(2);
        for (Node graphNode : graph.getAllNodes()) {
            tmpArr[0] = graphNode.getLatitude();
            tmpArr[1] = graphNode.getLongitude();
            kdTree.insert(tmpArr, graphNode.id);
        }
//        ingoingKDTreeMap = createIngoingKDTreeMap();
//        outgoingKDTreeMap = createOutgoingKDTreeMap();

        logger.info("KDTree created");
    }


    private Map<TransportMode, KDTree> createIngoingKDTreeMap() {
        Map<TransportMode, KDTree> map = new HashMap<>();
        double[] tmpArr = new double[2];
        Arrays.stream(TransportMode.availableModes())
                .forEach(transportMode -> {
                    KDTree tmpKdTree = new KDTree(2);
                    graph.getAllNodes()
                            .stream()
                            .filter(graphNode -> nodeIncomingModes.get(graphNode.id) != null
                                    && nodeIncomingModes.get(graphNode.id).contains(transportMode)
                            )
                            .forEach(graphNode -> {
                                tmpArr[0] = graphNode.getLatitude();
                                tmpArr[1] = graphNode.getLongitude();
                                tmpKdTree.insert(tmpArr, graphNode.id);
                            });
                    map.put(transportMode, tmpKdTree);
                });
        return map;
    }

    private Map<TransportMode, KDTree> createOutgoingKDTreeMap() {
        Map<TransportMode, KDTree> map = new HashMap<>();
        double[] tmpArr = new double[2];

        Arrays.stream(TransportMode.availableModes())
                .forEach(transportMode -> {
                    KDTree tmpKdTree = new KDTree(2);
                    graph.getAllNodes()
                            .stream()
                            .filter(graphNode -> nodeOutcomingModes.get(graphNode.id) != null
                                    && nodeOutcomingModes.get(graphNode.id).contains(transportMode))
                            .forEach(graphNode -> {
                                tmpArr[0] = graphNode.getLatitude();
                                tmpArr[1] = graphNode.getLongitude();
                                tmpKdTree.insert(tmpArr, graphNode.id);
                            });
                    map.put(transportMode, tmpKdTree);
                });

        return map;
    }

    public KDTree getKdTree() {
        return kdTree;
    }

    public KDTree getKdTreeForMode(TransportMode mode, boolean isIngoingMode) {
        return isIngoingMode ? ingoingKDTreeMap.get(mode) : outgoingKDTreeMap.get(mode);
    }


    public void createGraphFromKnownRequests(int numOfRequests) {
        createGraph(getRoutesFromKnownRequest(numOfRequests));
    }

    public void createGraphFromUnknownRequests(int numOfRequests) {
        createGraphFromUnknownRequests(numOfRequests, GraphExpansionStrategy.RANDOM_OD);
    }

    public void createGraphFromUnknownRequests(int numOfRequests, GraphExpansionStrategy strategy) {
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (TransportMode mode : TransportMode.availableModes()) {
            // Uncomment for loop for generating more routes
            routeList = expandGraph(numOfRequests, mode, strategy);
            routes.addAll(routeList);
        }

        createGraph(routes);
    }

    private List<Route> getRoutesFromKnownRequest(int numOfRequests) {
        List<Route> resultRoutes = new ArrayList<>();
        List<Route> tmpResult;
        for (TransportMode mode : TransportMode.availableModes()) {
            for (int i = 1; i <= numOfRequests; i++) {
                tmpResult = null;
                if (mode == TransportMode.TRANSIT || mode == TransportMode.BICYCLE) {
                    tmpResult = OTPlannerAdapter.getInstance().findRoutesFromKnownRequests(i, mode);
                } else if (mode == TransportMode.CAR || mode == TransportMode.WALK) {
                    tmpResult = GMapsPlannerAdapter.getInstance().findRoutesFromKnownRequests(i, mode);
                }
                if (tmpResult == null) continue;

                resultRoutes.addAll(tmpResult);
            }
        }
        return resultRoutes;
    }

    private List<Route> expandGraph(int numOfRequests, PlannerAdapter plannerAdapter) {
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

    private List<Route> expandGraph(int numOfRequests, TransportMode mode, GraphExpansionStrategy strategy) {
        switch (mode) {
            case BICYCLE:
            case TRANSIT:
                return expandGraph(numOfRequests, OTPlannerAdapter.getInstance(), mode, strategy);
            default:
                return expandGraph(numOfRequests, GMapsPlannerAdapter.getInstance(), mode, strategy);
        }
    }

    private List<Route> expandGraph(int numOfRequests, PlannerAdapter plannerAdapter,
                                    TransportMode mode, GraphExpansionStrategy strategy) {
        switch (strategy) {
            case RANDOM_OD:
                return expandGraphByRandomOD(numOfRequests, plannerAdapter, mode);
            case CHAINING_RANDOM_OD:
                return expandGraphByChainingRandomOD(numOfRequests, plannerAdapter, mode);
            case RANDOM_OD_WITH_MIN_DISTANCE_BETWEEN:
                return expandGraphByRandomODWithMinDistanceBetween(numOfRequests, plannerAdapter, mode);
            case NODES_MIN_COVERAGE_EQ_DIST:
                return expandGraphByFillingMinNodesAreaEqDist(numOfRequests, plannerAdapter, mode);
            case NODES_MIN_COVERAGE_NORM_DIST:
                return expandGraphByFillingMinNodesAreaNormDist(numOfRequests, plannerAdapter, mode);
            case EDGES_MIN_COVERAGE_EQ_DIST:
                return expandGraphByFillingMinEdgesAreaEqDist(numOfRequests, plannerAdapter, mode);
            case EDGES_MIN_COVERAGE_NORM_DIST:
                return expandGraphByFillingMinEdgesAreaNormDist(numOfRequests, plannerAdapter, mode);
            case USING_KNOWN_NODES_AS_OD:
                return expandGraphUsingKnownNodesAsOD(numOfRequests, plannerAdapter, mode);
            case RANDOM_OD_WITH_KNOWN_NODES_BETWEEN:
                return expandGraphUsingKnownNodesBetweenOD(numOfRequests, plannerAdapter, mode);
        }

        return null;
    }

    private List<Route> expandGraphByRandomOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                              TransportMode mode) {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (int i = 0; i < numOfRequests; i++) {
            locArray = Location.generateRandomLocationsInPrague(2);
            routeList = plannerAdapter.findRoutes(locArray[0], locArray[1], mode);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locArray[1], locArray[0], mode);
            routes.addAll(routeList);
        }

        return routes;
    }

    private List<Route> expandGraphByChainingRandomOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                                      TransportMode mode) {
        Location locFrom, locTo;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        locFrom = Location.generateRandomLocationInPrague();

        for (int i = 0; i < numOfRequests; i++) {
            locTo = Location.generateRandomLocationInPrague();
            routeList = plannerAdapter.findRoutes(locFrom, locTo, mode);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locTo, locFrom, mode);
            routes.addAll(routeList);
        }

        return routes;
    }

    private List<Route> expandGraphByRandomODWithMinDistanceBetween(int numOfRequests, PlannerAdapter plannerAdapter,
                                                                    TransportMode mode) {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (int i = 0; i < numOfRequests; i++) {
            locArray = Location.generateRandomLocationsInPragueWithMinDistanceBetween(2);
            routeList = plannerAdapter.findRoutes(locArray[0], locArray[1], mode);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locArray[1], locArray[0], mode);
            routes.addAll(routeList);
        }

        return routes;
    }

    private List<Route> expandGraphByFillingMinNodesAreaNormDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                                 TransportMode mode) {
        return null;
    }

    private List<Route> expandGraphByFillingMinNodesAreaEqDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                               TransportMode mode) {
        return null;
    }

    private List<Route> expandGraphByFillingMinEdgesAreaEqDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                               TransportMode mode) {
        return null;
    }

    private List<Route> expandGraphByFillingMinEdgesAreaNormDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                                 TransportMode mode) {
        return null;
    }

    private List<Route> expandGraphUsingKnownNodesAsOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                                       TransportMode mode) {
        return null;
    }

    private List<Route> expandGraphUsingKnownNodesBetweenOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                                            TransportMode mode) {
        return null;
    }
}
