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
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

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


    public Graph<Node, GraphEdge> createGraphFromKnownRequests(int numOfRequests) {
        return createGraph(expandGraphFromKnownRequests(numOfRequests));
    }

    public Graph<Node, GraphEdge> createGraphFromUnknownRequests(int numOfRequests, LocationArea locationArea) {
        return createGraphFromUnknownRequests(numOfRequests, locationArea, GraphExpansionStrategy.RANDOM_OD);
    }

    public Graph<Node, GraphEdge> createGraphFromUnknownRequests(int numOfRequests, LocationArea locationArea,
                                                                 GraphExpansionStrategy strategy) {
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (TransportMode mode : TransportMode.availableModes()) {
            // Uncomment for loop for generating more routes
            routeList = expandGraph(numOfRequests, locationArea, mode, strategy);
            routes.addAll(routeList);
        }

        return createGraph(routes);
    }

    private List<Route> expandGraphFromKnownRequests(int numOfRequests) {
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

    private List<Route> expandGraph(int numOfRequests, LocationArea locationArea, GraphExpansionStrategy strategy) {
        List<Route> routeList = new ArrayList<>();
        for (TransportMode mode : TransportMode.availableModes()) {
            routeList.addAll(expandGraph(numOfRequests, locationArea, mode, strategy));
        }
        return routeList;
    }

    private List<Route> expandGraph(int numOfRequests, LocationArea locationArea,
                                    TransportMode mode, GraphExpansionStrategy strategy) {
        switch (mode) {
            case BICYCLE:
            case TRANSIT:
                return expandGraph(numOfRequests, OTPlannerAdapter.getInstance(), mode, locationArea, strategy);
            default:
                return expandGraph(numOfRequests, GMapsPlannerAdapter.getInstance(), mode, locationArea, strategy);
        }
    }

    private List<Route> expandGraph(int numOfRequests, PlannerAdapter plannerAdapter,
                                    TransportMode mode, LocationArea locationArea, GraphExpansionStrategy strategy) {
        switch (strategy) {
            case RANDOM_OD:
                return expandGraphByRandomOD(numOfRequests, plannerAdapter, mode, locationArea);
            case CHAINING_RANDOM_OD:
                return expandGraphByChainingRandomOD(numOfRequests, plannerAdapter, mode, locationArea);
            case RANDOM_OD_WITH_MIN_DISTANCE_BETWEEN:
                return expandGraphByRandomODWithMinDistanceBetween(numOfRequests, plannerAdapter, mode, locationArea);
            case NODES_MIN_COVERAGE_EQ_DIST:
                return expandGraphByFillingMinNodesAreaEqDist(numOfRequests, plannerAdapter, mode, locationArea);
            case NODES_MIN_COVERAGE_NORM_DIST:
                return expandGraphByFillingMinNodesAreaNormDist(numOfRequests, plannerAdapter, mode, locationArea);
            case EDGES_MIN_COVERAGE_EQ_DIST:
                return expandGraphByFillingMinEdgesAreaEqDist(numOfRequests, plannerAdapter, mode, locationArea);
            case EDGES_MIN_COVERAGE_NORM_DIST:
                return expandGraphByFillingMinEdgesAreaNormDist(numOfRequests, plannerAdapter, mode, locationArea);
            case USING_KNOWN_NODES_AS_OD:
                return expandGraphUsingKnownNodesAsOD(numOfRequests, plannerAdapter, mode, locationArea);
            case RANDOM_OD_WITH_KNOWN_NODES_BETWEEN:
                return expandGraphUsingKnownNodesBetweenOD(numOfRequests, plannerAdapter, mode, locationArea);
        }

        return null;
    }

    private List<Route> expandGraphByRandomOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                              TransportMode mode, LocationArea locationArea) {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (int i = 0; i < numOfRequests; i++) {
            locArray = locationArea.generateRandomLocations(2);
            routeList = plannerAdapter.findRoutes(locArray[0], locArray[1], mode);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locArray[1], locArray[0], mode);
            routes.addAll(routeList);
        }

        return routes;
    }

    private List<Route> expandGraphByChainingRandomOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                                      TransportMode mode, LocationArea locationArea) {
        Location locFrom, locTo;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        locFrom = locationArea.generateRandomLocation();

        for (int i = 0; i < numOfRequests; i++) {
            locTo = locationArea.generateRandomLocation();
            routeList = plannerAdapter.findRoutes(locFrom, locTo, mode);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locTo, locFrom, mode);
            routes.addAll(routeList);

            locFrom = locTo;
        }

        return routes;
    }

    private List<Route> expandGraphByRandomODWithMinDistanceBetween(int numOfRequests, PlannerAdapter plannerAdapter,
                                                                    TransportMode mode, LocationArea locationArea) {
        Location[] locArray;
        List<Route> routes = new ArrayList<>();
        List<Route> routeList;

        for (int i = 0; i < numOfRequests; i++) {
            locArray = locationArea.generateODWithMinimalDistanceBetween(Storage.MIN_DISTANCE_IN_METERS_BETWEEN_OD);
            routeList = plannerAdapter.findRoutes(locArray[0], locArray[1], mode);
            routes.addAll(routeList);
            routeList = plannerAdapter.findRoutes(locArray[1], locArray[0], mode);
            routes.addAll(routeList);
        }

        return routes;
    }

    private List<Route> expandGraphByFillingMinNodesAreaNormDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                                 TransportMode mode, LocationArea locationArea) {
        int remainingRequestsCount = numOfRequests;
        List<Route> tmpRouteList = new ArrayList<>();
        Graph<Node, GraphEdge> tmpGraph;
        while (remainingRequestsCount > 0) {
            tmpRouteList.addAll(expandGraph(500, locationArea, GraphExpansionStrategy.RANDOM_OD));
            tmpGraph = createGraph(tmpRouteList);
            //TODO: compare ideal norm distribution and current distribution
            //TODO: according to comparision fill "empty places"
        }

        return tmpRouteList;
    }

    private List<Route> expandGraphByFillingMinNodesAreaEqDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                               TransportMode mode, LocationArea locationArea) {
        int remainingRequestsCount = numOfRequests;
        List<Route> tmpRouteList = new ArrayList<>();
        Graph<Node, GraphEdge> tmpGraph;
        while (remainingRequestsCount > 0) {
            tmpRouteList.addAll(expandGraph(500, locationArea, GraphExpansionStrategy.RANDOM_OD));
            tmpGraph = createGraph(tmpRouteList);
            List<LocationArea> invalidAreaList = checkNodesEqualDistribution(tmpGraph, locationArea);
            //TODO: compare ideal equal distribution and current distribution
            //TODO: according to comparision fill "empty places"
        }

        return tmpRouteList;
    }

    private int[][] createNormalDistributionOnGrid(LocationArea[][] areaGrid, int numOfNodes) {
        int gridRowsCount = areaGrid.length;
        int gridColumnsCount = areaGrid[0].length;

        int[][] gridNormDistribution = new int[gridRowsCount][gridColumnsCount];

        double[] means = new double[]{0, 0};
        double[][] covariances = new double[][]{{1, 0}, {0, 1}};
        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(means, covariances);

        double rowStepSize = 4 / (double) gridRowsCount;
        double columnStepSize = 4 / (double) gridColumnsCount;


        double val_j;
        double val_i = -2;

        for (int i = 0; i < gridRowsCount; i++, val_i += rowStepSize) {
            val_j = -2;
            for (int j = 0; j < gridColumnsCount; j++, val_j += columnStepSize) {
                gridNormDistribution[i][j] = (int) (numOfNodes * distribution.density(new double[]{val_i, val_j}));
            }
        }

        return gridNormDistribution;
    }

    private List<LocationArea> getInvalidAreasFromMask(boolean[][] mask, LocationArea[][] areaGrid) {
        List<LocationArea> invalidLocArr = new ArrayList<>();
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                if (mask[i][j]) invalidLocArr.add(areaGrid[i][j]);
            }
        }

        return invalidLocArr;
    }

    private List<LocationArea> checkNodesEqualDistribution(Graph<Node, GraphEdge> tmpGraph, LocationArea locationArea) {

        int gridX = Storage.GRAPH_DISTRIBUTION_GRID_X;
        int gridY = Storage.GRAPH_DISTRIBUTION_GRID_Y;
        List<Node> nodeList = (List<Node>) tmpGraph.getAllNodes();
        int numOfNodes = nodeList.size();
        int numOfNodesPerCell = numOfNodes / gridX * gridY;

        LocationArea[][] areaGrid = locationArea.createGrid(gridX, gridY);

        int[][] equalDistribution = new int[gridX][gridY];
        for (int[] row : equalDistribution) {
            Arrays.fill(row, numOfNodesPerCell);
        }

        int[][] currentDistribution = getNodesDistributionOnGrid(nodeList, areaGrid);

        boolean[][] mask = checkIfDistributionIsValid(currentDistribution, equalDistribution);

        return getInvalidAreasFromMask(mask, areaGrid);
    }

    private List<LocationArea> checkNodesNormalDistribution(Graph<Node, GraphEdge> tmpGraph, LocationArea locationArea) {
        int gridX = Storage.GRAPH_DISTRIBUTION_GRID_X;
        int gridY = Storage.GRAPH_DISTRIBUTION_GRID_Y;
        List<Node> nodeList = (List<Node>) tmpGraph.getAllNodes();
        int numOfNodes = nodeList.size();

        LocationArea[][] areaGrid = locationArea.createGrid(gridX, gridY);

        int[][] normDistribution = createNormalDistributionOnGrid(areaGrid, numOfNodes);
        int[][] currentDistribution = getNodesDistributionOnGrid(nodeList, areaGrid);

        boolean[][] mask = checkIfDistributionIsValid(currentDistribution, normDistribution);

        return getInvalidAreasFromMask(mask, areaGrid);

    }

    private boolean[][] checkIfDistributionIsValid(int[][] distribution, int[][] patternDistribution) {
        boolean[][] mask = new boolean[distribution.length][distribution[0].length];

        for (int i = 0; i < distribution.length; i++) {
            for (int j = 0; j < distribution[0].length; j++) {
                int diff = distribution[i][j] - patternDistribution[i][j];
                mask[i][j] = diff > 0;
            }
        }

        return mask;
    }

    private int[][] getNodesDistributionOnGrid(List<Node> nodeList, LocationArea[][] areaGrid) {
        if (areaGrid == null || areaGrid[0].length == 0) return null;

        LocationArea tmpCell;
        int tmpRow, tmpColumn;
        int[][] nodeDistribution = new int[areaGrid.length][areaGrid[0].length];

        for (int[] row : nodeDistribution) {
            Arrays.fill(row, 0);
        }

        for (Node node : nodeList) {
            tmpRow = tmpColumn = 0;
            Location nodeLocation = Location.getLocation(node);
            while (true) {
                tmpCell = areaGrid[tmpRow][tmpColumn];
                if (tmpCell.containsLocation(nodeLocation)) {
                    nodeDistribution[tmpRow][tmpColumn]++;
                    break;
                } else {
                    if (nodeLocation.lat < tmpCell.bottomLat) {
                        tmpRow++;
                    }
                    if (nodeLocation.lon > tmpCell.rightLon) {
                        tmpColumn++;
                    }
                }
            }
        }
        return nodeDistribution;
    }

    private List<Route> expandGraphByFillingMinEdgesAreaEqDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                               TransportMode mode, LocationArea locationArea) {
        return null;
    }

    private List<Route> expandGraphByFillingMinEdgesAreaNormDist(int numOfRequests, PlannerAdapter plannerAdapter,
                                                                 TransportMode mode, LocationArea locationArea) {
        return null;
    }

    private List<Route> expandGraphUsingKnownNodesAsOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                                       TransportMode mode, LocationArea locationArea) {
        return null;
    }

    private List<Route> expandGraphUsingKnownNodesBetweenOD(int numOfRequests, PlannerAdapter plannerAdapter,
                                                            TransportMode mode, LocationArea locationArea) {
        return null;
    }
}
