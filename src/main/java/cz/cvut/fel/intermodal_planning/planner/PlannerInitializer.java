package cz.cvut.fel.intermodal_planning.planner;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Main;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class PlannerInitializer {
    public GraphMaker perfectGraphMaker;
    public GraphMaker extendedGraphMaker;

    public RoutePlanner perfectRoutePlanner;
    public RoutePlanner extendedRoutePlanner;

    public PlannerInitializer() {
        initGraph();
        createKdTrees();
    }

    private void initGraph() {
        try {
            File perfectGraphFile = Paths.get(Storage.GRAPH_RESOURCE.toURI()).toFile();
            File extendedGraphFile = Paths.get(Storage.GRAPH_EXTENDED_RESOURCE.toURI()).toFile();

            Graph<Node, GraphEdge> perfectGraph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(perfectGraphFile);
            Graph extendedGraph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(extendedGraphFile);

            perfectGraphMaker = new GraphMaker();
            extendedGraphMaker = new GraphMaker();

            perfectRoutePlanner = new RoutePlanner(perfectGraphMaker);
            extendedRoutePlanner = new RoutePlanner(extendedGraphMaker);

            Main.EXTENDED = false;
            if (perfectGraph == null) {
                perfectRoutePlanner.expandGraphFromKnownRequests(15000);
                SerializationUtils.writeObjectToFile(perfectRoutePlanner.getGraph(), perfectGraphFile);
            } else {
                perfectGraphMaker.setGraph(perfectGraph);
            }
            Main.EXTENDED = true;
            if (extendedGraph == null) {
                extendedRoutePlanner.expandGraphFromKnownRequests(20000);
                SerializationUtils.writeObjectToFile(extendedRoutePlanner.getGraph(), extendedGraphFile);
            } else {
                extendedGraphMaker.setGraph(extendedGraph);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void createKdTrees() {
        perfectGraphMaker.createKDTree();
        extendedGraphMaker.createKDTree();
    }
}
