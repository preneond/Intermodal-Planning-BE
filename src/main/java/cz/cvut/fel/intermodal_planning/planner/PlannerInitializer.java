package cz.cvut.fel.intermodal_planning.planner;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.GraphMaker;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphExpansionStrategy;
import cz.cvut.fel.intermodal_planning.graph.model.GraphEdge;
import cz.cvut.fel.intermodal_planning.planner.model.LocationArea;
import cz.cvut.fel.intermodal_planning.planner.model.Route;
import cz.cvut.fel.intermodal_planning.general.utils.SerializationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class PlannerInitializer {
    private static final Logger logger = LogManager.getLogger(PlannerInitializer.class);
    private static PlannerInitializer sharedInstance;

    public GraphExpansionStrategy expansionStrategy;
    public LocationArea locationArea;
    private List<Route> routeList;

    public GraphMaker graphMaker;
    public RoutePlanner routePlanner;
    public int requestCount = 0;


    public PlannerInitializer(LocationArea locationArea) {
        this.locationArea = locationArea;

        graphMaker = new GraphMaker();
        routeList = new ArrayList<>();
    }

    /**
     * create instance from stored requests
     *
     * @return PlannerInitializer instance
     */
    public static PlannerInitializer getKnownInstance() {
        if (sharedInstance == null) {
            sharedInstance = new PlannerInitializer();
        }
        return sharedInstance;
    }

    private PlannerInitializer() {
        expansionStrategy = GraphExpansionStrategy.RANDOM_OD;
        locationArea = Storage.AREA_PRAGUE;
        graphMaker = new GraphMaker();
        routeList = new ArrayList<>();

        initPlannerUsingKnownGraph();
    }

    /**
     *
     * @param requestCount Number Of Subplanner Requests
     * @param expansionStrategy Strategy of picking OD Pair
     * @return RoutePlanner instance
     */
    public RoutePlanner initPlanner(int requestCount, GraphExpansionStrategy expansionStrategy) {
        if (requestCount > this.requestCount) {
            int numOfRequest = requestCount -  this.requestCount;
            this.requestCount = requestCount;
            List<Route> graphExpansionList = graphMaker.expandGraph(numOfRequest, locationArea, expansionStrategy);
            routeList.addAll(graphExpansionList);
            graphMaker.createGraph(routeList);
        } else {
            graphMaker.createGraph(routeList.subList(0, requestCount));
        }

        graphMaker.createKDTree();
        routePlanner = new RoutePlanner(graphMaker);

        return routePlanner;
    }

    /**
     * Planner initialization, for which the known requests are used
     */
    public void initPlannerUsingKnownGraph() {
        try {
            File graphFile = Paths.get(Storage.GRAPH_RESOURCE.toURI()).toFile();
            Graph<Node, GraphEdge> graph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromResources("graph.json");

            if (graph == null) {
                logger.error("Graph is NULL");
                graphMaker.createGraphFromKnownRequests(Storage.KNOWN_REQUEST_COUNT);
                SerializationUtils.writeObjectToFile(graphMaker.getGraph(), graphFile);
            } else {
                logger.info("Graph is serialized successfully");
                graphMaker.setGraph(graph);
            }
            graphMaker.createKDTree();
            routePlanner = new RoutePlanner(graphMaker);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
