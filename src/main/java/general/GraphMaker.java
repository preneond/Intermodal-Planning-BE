package general;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.GraphBuilder;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import pathfinding.kdtree.KDTree;

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
        getGraphDescription();
        nodeCounter = 0;

        return graph;
    }


    public void setGraph(Graph<Node, GraphEdge> graph) {
        addNodes(graph.getAllNodes());
        addEdgeCollection(graph.getAllEdges());
        this.graph = createGraph();
        getGraphDescription();
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


    public void getGraphDescription() {
        Collection<Node> nodeList = graph.getAllNodes();
        Collection<GraphEdge> edgeList = graph.getAllEdges();

        List carList = edgeList.stream().filter(graphEdge -> graphEdge.mode == TransportMode.CAR).collect(Collectors.toList());
        List walkingList = edgeList.stream().filter(graphEdge -> graphEdge.mode == TransportMode.WALK).collect(Collectors.toList());
        List transitList = edgeList.stream().filter(graphEdge -> graphEdge.mode == TransportMode.TRANSIT).collect(Collectors.toList());
        List bikeList = edgeList.stream().filter(graphEdge -> graphEdge.mode == TransportMode.BICYCLE).collect(Collectors.toList());

        int inputLevel = 0;
        int outputLevel = 0;

        for (Node node : nodeList) {
            inputLevel += graph.getInEdges(node.id) == null ? 0 : graph.getInEdges(node.id).size();
            outputLevel += graph.getOutEdges(node.id) == null ? 0 : graph.getOutEdges(node.id).size();
        }

        double avgInputLevel = inputLevel / (double) nodeList.size();
        double avgOutputLevel = outputLevel / (double) nodeList.size();

        logger.info("\n" +
                "Number of requests: " + Main.numOfRequests + "\n" +
                "Number of edges: " + edgeList.size() + "\n" +
                "Number of nodes: " + nodeList.size() + "\n" +
                "Number of edges for walking: " + walkingList.size() + "\n" +
                "Number of edges for bike: " + bikeList.size() + "\n" +
                "Number of edges for public transport: " + transitList.size() + "\n" +
                "Number of edges for car: " + carList.size() + "\n" +
                "Average input level:  " + avgInputLevel + "\n" +
                "Average output level: " + avgOutputLevel);
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
        ingoingKDTreeMap = createIngoingKDTreeMap();
        outgoingKDTreeMap = createOutgoingKDTreeMap();

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
}
