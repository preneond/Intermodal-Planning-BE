package general;

import com.umotional.basestructures.Edge;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.GraphBuilder;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Ondrej Prenek on 27/07/2017.
 * This code is owned by Umotional s.r.o. (IN: 03974618).
 * All Rights Reserved.
 */
public class GraphMaker extends GraphBuilder {
    private static GraphMaker sharedInstance;
    private Graph<Node, GraphEdge> graph;
    private int nodeCounter = 0;

    private static final Logger logger = LogManager.getLogger(GraphMaker.class);

    public static GraphMaker getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new GraphMaker();
        }
        return sharedInstance;
    }

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

    public void setGraph(Graph<Node, GraphEdge> graph) {
        addNodes(graph.getAllNodes());
        addEdges(graph.getAllEdges());

        nodeCounter = graph.getAllNodes().size();
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
                    edge.mode = leg.mode;
                    edge.durationInSeconds = leg.durationInSeconds;
                    addEdge(edge);
                }
            } else addSteps(leg.steps);
        }
    }

    private void addSteps(List<Step> steps) {
        for (Step step : steps) {
            if (step.steps != null) {
                this.addSteps(step.steps);
                continue;
            }
            int startId = getIdFor(step.startLocation);
            int endId = getIdFor(step.endLocation);
            if (!containsEdge(startId, endId)) {
                GraphEdge edge = new GraphEdge(startId, endId, (int) step.durationInSeconds);
                edge.mode = step.transportMode;
//                edge.polyline = step.polyline;
                edge.durationInSeconds = step.durationInSeconds;
                addEdge(edge);
            }
        }
    }


    private void getGraphDescription() {
        Collection<Node> nodeList = graph.getAllNodes();
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
     * @param location - location which is unique for each node
     * @return id for node on given location
     */
    private int getIdFor(Location location) {
        int id;
        try {
            id = getIntIdForSourceId(generateSourceIdFor(location));
        } catch (NullPointerException e) {
            id = nodeCounter;
            Node startNode = new Node(nodeCounter, generateSourceIdFor(location), location.lat, location.lon, location.latE3(), location.lonE3(), 0);
            addNode(startNode);
            nodeCounter++;
        }
        return id;
    }

    /**
     * Create unique Node source id based on location using Szudzik's function
     *
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
}
