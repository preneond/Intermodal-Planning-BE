

import com.umotional.basestructures.Edge;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.GraphBuilder;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.*;

import java.util.Collection;
import java.util.List;

/**
 * Created by Ondrej Prenek on 27/07/2017.
 * This code is owned by Umotional s.r.o. (IN: 03974618).
 * All Rights Reserved.
 */
public class GraphMaker extends GraphBuilder {
    private static GraphMaker sharedInstance;
    private Graph<Node, GraphEdge> graph;
    private int nodeCounter = 0;

    public static GraphMaker getInstance() {
        if (sharedInstance == null)
            sharedInstance = new GraphMaker();
        return sharedInstance;
    }

    public Graph createGraph(List<Route> routeList) {
        System.out.println("Creating graph...");
        addRoutes(routeList);
        graph = createGraph();
        System.out.println("Graph created.");
        getGraphDescription();

        return graph;
    }

    private void addRoutes(List<Route> routes) {
        for (Route route : routes) {
            addLegs(route.legList);
        }
    }

    private void addLegs(List<Leg> legs) {
        for (Leg leg : legs) {
            addSteps(leg.steps);
        }
    }

    private void addSteps(List<Step> steps) {
        for (Step step : steps) {
            if (step.steps != null) {
                this.addSteps(step.steps);
                return;
            }
            int startId = getIdFor(step.startLocation);
            int endId = getIdFor(step.endLocation);
            if (!containsEdge(startId, endId)) {
                Edge edge = new GraphEdge(startId, endId, (int) step.distanceInMeters);
                Edge reverEdge = new GraphEdge(endId, startId, (int) step.distanceInMeters);
                addEdge(edge);
                addEdge(reverEdge);
            }
        }

    }


    private void getGraphDescription() {
        graph.getAllEdges();

        Collection<Node> nodeList = graph.getAllNodes();
        System.out.println("Number of nodes:" + nodeList.size());

        Collection<GraphEdge> edgeList = graph.getAllEdges();
        System.out.println("Number of edges:" + edgeList.size());

        int inputLevel = 0;
        int outputLevel = 0;

        for (Node node : nodeList) {
            inputLevel += graph.getInEdges(node.id) == null ? 0 : graph.getInEdges(node.id).size();
            outputLevel += graph.getOutEdges(node.id) == null ? 0 : graph.getOutEdges(node.id).size();
        }

        double avgInputLevel = inputLevel / (double) nodeList.size();
        double avgOutputLevel = outputLevel / (double) nodeList.size();

        System.out.println("Average input level:  " + avgInputLevel);
        System.out.println("Average output level: " + avgOutputLevel);

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
     * Create unique Node source id based on location
     *
     * @param location - location which is unique for each node
     * @return unique sourceId for given location
     */
    private int generateSourceIdFor(Location location) {
        return location.latE6() + location.lonE6();
    }
}
