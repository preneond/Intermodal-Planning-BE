package model.graph;

import com.umotional.basestructures.Edge;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.GraphBuilder;
import com.umotional.basestructures.Node;
import model.planner.Leg;
import model.planner.Location;
import model.planner.Route;
import model.planner.Step;

import java.util.ArrayList;
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

    public static GraphMaker getInstance() {
        if (sharedInstance == null)
            sharedInstance = new GraphMaker();
        return sharedInstance;
    }

    public Graph createGraph(List<Route> routeList) {
        System.out.println("Creating graph...");
        fillGraph(routeList);
        graph = createGraph();
        System.out.println("Graph created.");
        getGraphDescription();

        return graph;
    }

    private void fillGraph(List<Route> routeList) {
        int nodeCounter = 0;
        int startId = 0;
        int endId = 0;
        for (Route route : routeList) {
            for (Leg leg : route.legList) {
                for (Step step : leg.steps) {

                    Location startLoc = step.startLocation;
                    //noinspection Duplicates
                    try {
                        startId = getIntIdForSourceId(generateIdFor(startLoc));
                    } catch (NullPointerException e) {
                        startId = nodeCounter;
                        Node startNode = new Node(nodeCounter, generateIdFor(startLoc), startLoc.lat, startLoc.lon, startLoc.latE3, startLoc.lonE3, 0);
                        addNode(startNode);
                        nodeCounter++;
                    }

                    Location endLoc = step.endLocation;
                    //noinspection Duplicates
                    try {
                        endId = getIntIdForSourceId(generateIdFor(endLoc));
                    } catch (NullPointerException e) {
                        endId = nodeCounter;
                        Node endNode = new Node(nodeCounter, generateIdFor(endLoc), startLoc.lat, startLoc.lon, startLoc.latE3, startLoc.lonE3, 0);
                        addNode(endNode);
                        nodeCounter++;
                    }

                    Edge edge = new GraphEdge(startId, endId, (int) step.distanceInMeters);
                    addEdge(edge);
                }
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
     * Create unique Node id based on location
     */
    public int generateIdFor(Location location) {
        return location.latE3+location.lonE3;
    }
}
