package general;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.GraphBuilder;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.graph.GraphNode;
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
    private Graph<GraphNode, GraphEdge> graph;
    private KDTree kdTree;

    private Map<TransportMode, KDTree> ingoingKDTreeMap;
    private Map<TransportMode, KDTree> outgoingKDTreeMap;
    private int nodeCounter = 0;

    private static final Logger logger = LogManager.getLogger(GraphMaker.class);

    public Graph getGraph() {
        return graph;
    }

    public Graph createGraph(List<Route> routeList) {
        addRoutes(routeList);

        graph = createGraph();
        getGraphDescription();
        nodeCounter = 0;

        return graph;
    }

    public void setGraph(Graph<GraphNode, GraphEdge> graph, boolean append) {
        if (append) {
            addNodes(graph.getAllNodes());
            addEdges(graph.getAllEdges());

            nodeCounter = graph.getAllNodes().size();
        } else {
            this.graph = graph;
        }
    }

    private void addRoutes(List<Route> routes) {
        for (Route route : routes) {
            addLegs(route.legList);
        }
    }

    private void addLegs(List<Leg> legs) {
        for (Leg leg : legs) {
            if (leg.steps.isEmpty()) {
                int startId = getIdFor(leg.startLocation, leg.transportMode, false);
                int endId = getIdFor(leg.endLocation, null, true);
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
            int startId = getIdFor(step.startLocation, step.transportMode, false);
            int endId = getIdFor(step.endLocation, null, true);
            if (!containsEdge(startId, endId)) {
                GraphEdge edge = new GraphEdge(startId, endId, (int) step.durationInSeconds);
                edge.mode = step.transportMode;
                edge.durationInSeconds = step.durationInSeconds;
                addEdge(edge);
            }
        }
    }


    public void getGraphDescription() {
        Collection<GraphNode> nodeList = graph.getAllNodes();
        Collection<GraphEdge> edgeList = graph.getAllEdges();

        List carList = edgeList.stream().filter(graphEdge -> graphEdge.mode == TransportMode.CAR).collect(Collectors.toList());
        List walkingList = edgeList.stream().filter(graphEdge -> graphEdge.mode == TransportMode.WALK).collect(Collectors.toList());
        List transitList = edgeList.stream().filter(graphEdge -> graphEdge.mode == TransportMode.TRANSIT).collect(Collectors.toList());

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
                "Number of edges for car: " + carList.size() + "\n" +
                "Number of edges for walking: " + walkingList.size() + "\n" +
                "Number of edges for public transport: " + transitList.size() + "\n" +
                "Average input level:  " + avgInputLevel + "\n" +
                "Average output level: " + avgOutputLevel);
    }

    /**
     * Method which return id for current location and create node whether ain't exists.
     *
     * @param location       - location which is unique for each node
     * @param transportMode
     * @param isOutgoingMode
     * @return id for node on given location
     */
    private int getIdFor(Location location, TransportMode transportMode, boolean isOutgoingMode) {
        int id;
        GraphNode node;
        try {
            id = getIntIdForSourceId(generateSourceIdFor(location));
            node = (GraphNode) getNode(id);
        } catch (NullPointerException e) {
            id = nodeCounter;
            node = new GraphNode(nodeCounter, generateSourceIdFor(location), location.lat, location.lon,
                    location.latE3(), location.lonE3(), 0);
            addNode(node);
            nodeCounter++;
        }
        if (isOutgoingMode) {
            node.outgoingModes.add(transportMode);
        } else {
            node.ingoingModes.add(transportMode);
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
        for (GraphNode graphNode : graph.getAllNodes()) {
            tmpArr[0] = graphNode.getLatitude();
            tmpArr[1] = graphNode.getLongitude();
            kdTree.insert(tmpArr, graphNode.id);
        }
        ingoingKDTreeMap = new HashMap<>();
        Arrays.stream(TransportMode.values())
                .forEach(transportMode -> {
                    KDTree tmpKdTree = new KDTree(2);
                    graph.getAllNodes()
                            .stream()
                            .filter(graphNode -> graphNode.ingoingModes.contains(transportMode))
                            .forEach(graphNode -> {
                                tmpArr[0] = graphNode.getLatitude();
                                tmpArr[1] = graphNode.getLongitude();
                                tmpKdTree.insert(tmpArr, graphNode.id);
                            });
                    ingoingKDTreeMap.put(transportMode, tmpKdTree);
                });

        outgoingKDTreeMap = new HashMap<>();
        Arrays.stream(TransportMode.values())
                .forEach(transportMode -> {
                    KDTree tmpKdTree = new KDTree(2);
                    graph.getAllNodes()
                            .stream()
                            .filter(graphNode -> graphNode.outgoingModes.contains(transportMode))
                            .forEach(graphNode -> {
                                tmpArr[0] = graphNode.getLatitude();
                                tmpArr[1] = graphNode.getLongitude();
                                tmpKdTree.insert(tmpArr, graphNode.id);
                            });
                    outgoingKDTreeMap.put(transportMode, tmpKdTree);
                });
        logger.info("KDTree created");
    }

    public KDTree getKdTree() {
        return kdTree;
    }

    public KDTree getKdTreeForMode(TransportMode mode, boolean isIngoingMode) {
        return isIngoingMode ? ingoingKDTreeMap.get(mode): outgoingKDTreeMap.get(mode);
    }

//    public void setAvailableModes() {
//        Set<TransportMode> modes = new HashSet<>();
//        for (GraphNode node : graph.getAllNodes()) {
//            for (GraphEdge edge : graph.getOutEdges(node.id)) {
//                modes.add(edge.mode);
//            }
//            node.availableModes = new ArrayList<>(modes);
//        }
//    }
}
