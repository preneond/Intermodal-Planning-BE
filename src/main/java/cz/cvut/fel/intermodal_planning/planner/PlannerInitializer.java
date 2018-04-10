package cz.cvut.fel.intermodal_planning.planner;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class PlannerInitializer {
    public GraphMaker perfectGraphMaker;
//    public GraphMaker metaGraphMaker;

    public RoutePlanner perfectRoutePlanner;
//    public RoutePlanner metaRoutePlanner;

    public PlannerInitializer() {
        initGraph();
        createKdTrees();
    }

    private void initGraph() {
        try {
            File perfectGraphFile = Paths.get(Storage.GRAPH_RESOURCE.toURI()).toFile();
//            File metaGraphFile = Paths.get(Storage.METAGRAPH_RESOURCE.toURI()).toFile();

            Graph<Node,GraphEdge> perfectGraph = (Graph<Node,GraphEdge>) SerializationUtils.readObjectFromFile(perfectGraphFile);
//            Graph metaGraph = SerializationUtils.readGraphFromGeoJSON(metaGraphFile);

            perfectGraphMaker = new GraphMaker();
//            metaGraphMaker = new GraphMaker();

            perfectRoutePlanner = new RoutePlanner(perfectGraphMaker);
//            metaRoutePlanner = new RoutePlanner(metaGraphMaker);

            if (perfectGraph == null) {
//                perfectRoutePlanner.expandGraph(500, TransportMode.CAR);
                perfectRoutePlanner.expandGraphFromKnownRequests(10000);
                SerializationUtils.writeObjectToFile(perfectRoutePlanner.getGraph(), perfectGraphFile);
            } else {
                perfectGraphMaker.setGraph(perfectGraph);
            }
            /*
            if (metaGraph == null) {
                metaRoutePlanner.expandGraphFromKnownRequests(2000);
                SerializationUtils.writeObjectToFile(metaRoutePlanner.getGraph(), metaGraphFile);
            } else {
                metaGraphMaker.setGraph(metaGraph);
            }
            */

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void createKdTrees() {
        perfectGraphMaker.createKDTree();
//        metaGraphMaker.createKDTree();
    }
}
