package cz.cvut.fel.intermodal_planning.planner;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.GraphMaker;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphExpansionStrategy;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.LocationArea;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class PlannerInitializer {
    private static PlannerInitializer sharedInstance;
    private static final Logger logger = LogManager.getLogger(PlannerInitializer.class);

    public GraphMaker graphMaker;
    public RoutePlanner routePlanner;

    public static PlannerInitializer getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new PlannerInitializer();
        }
        return sharedInstance;
    }

    private PlannerInitializer() {
        initKnownGraph();
        graphMaker.createKDTree();
    }

    public PlannerInitializer(GraphExpansionStrategy strategy, LocationArea locationArea) {
        initGraph(strategy, locationArea);
        graphMaker.createKDTree();
    }

    private void initKnownGraph() {
        try {
            File graphFile = Paths.get(Storage.GRAPH_RESOURCE.toURI()).toFile();
            Graph<Node, GraphEdge> graph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(graphFile);

            graphMaker = new GraphMaker();
            routePlanner = new RoutePlanner(graphMaker);

            if (graph == null) {
                graphMaker.createGraphFromKnownRequests(15000);
                SerializationUtils.writeObjectToFile(graphMaker.getGraph(), graphFile);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void initGraph(GraphExpansionStrategy strategy, LocationArea locationArea) {
        graphMaker = new GraphMaker();
        graphMaker.createGraphFromUnknownRequests(1000, locationArea, strategy);

        routePlanner = new RoutePlanner(graphMaker);

}


}
