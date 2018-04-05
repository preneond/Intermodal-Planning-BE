package cz.cvut.fel.intermodal_planning.planner;

import com.umotional.basestructures.Graph;
import cz.cvut.fel.intermodal_planning.general.Constants;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class PlannerInitializer {
//    private static PlannerInitializer sharedInstance;

    private static GraphMaker perfectGraphMaker;
    private static GraphMaker metaGraphMaker;

    private static RoutePlanner perfectRoutePlanner;
    private static RoutePlanner metaRoutePlanner;


//    public static PlannerInitializer getInstance() {
//        if (sharedInstance == null) {
//            sharedInstance = new PlannerInitializer();
//        }
//        return sharedInstance;
//    }

    public PlannerInitializer() {
        initGraph();
        createKdTrees();
    }

    private void initGraph() {
        try {
            File perfectGraphFile = Paths.get(Constants.GRAPH_RESOURCE.toURI()).toFile();
            File metaGraphFile = Paths.get(Constants.METAGRAPH_RESOURCE.toURI()).toFile();

            Graph perfectGraph = SerializationUtils.readGraphFromGeoJSON(perfectGraphFile);
            Graph metaGraph = SerializationUtils.readGraphFromGeoJSON(metaGraphFile);

            perfectGraphMaker = new GraphMaker();
            metaGraphMaker = new GraphMaker();

            if (perfectGraph == null) {
                perfectGraphMaker.setGraph(perfectGraph);
            } else {
                perfectRoutePlanner.expandGraphFromKnownRequests(10000);
            }
            if (metaGraph == null) {
                metaGraphMaker.setGraph(metaGraph);
            } else {
                metaRoutePlanner.expandGraphFromKnownRequests(2000);
            }

            perfectRoutePlanner = new RoutePlanner(perfectGraphMaker);
            metaRoutePlanner = new RoutePlanner(metaGraphMaker);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void createKdTrees() {
        perfectGraphMaker.createKDTree();
        metaGraphMaker.createKDTree();
    }
}
