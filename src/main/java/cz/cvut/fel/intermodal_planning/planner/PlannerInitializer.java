package cz.cvut.fel.intermodal_planning.planner;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.graph.GraphMaker;
import cz.cvut.fel.intermodal_planning.graph.enums.GraphExpansionStrategy;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.LocationArea;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PlannerInitializer {
    private static final Logger logger = LogManager.getLogger(PlannerInitializer.class);
    private static PlannerInitializer sharedInstance;

    public GraphExpansionStrategy expansionStrategy;
    public LocationArea locationArea;
    private List<Route> routeList;

    public GraphMaker graphMaker;
    public RoutePlanner routePlanner;
    public int requestCount;


    PlannerInitializer(GraphExpansionStrategy expansionStrategy, LocationArea locationArea) {
        this.expansionStrategy = expansionStrategy;
        this.locationArea = locationArea;

        graphMaker = new GraphMaker();
        routeList = new ArrayList<>();
    }


    public static PlannerInitializer getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new PlannerInitializer();
        }
        return sharedInstance;
    }

    private PlannerInitializer() {
        initPlannerUsingKnownGraph();
    }

    public void initPlanner(int requestCount) {
        this.requestCount = requestCount;

        if (requestCount > routeList.size()) {
            int numOfRequest = requestCount - routeList.size();
            List<Route> graphExpansionList = graphMaker.expandGraph(numOfRequest, locationArea, expansionStrategy);
            routeList.addAll(graphExpansionList);
            graphMaker.createGraph(routeList);
        } else {
            graphMaker.createGraph(routeList.subList(0, requestCount));
        }

        graphMaker.createKDTree();
        routePlanner = new RoutePlanner(graphMaker);
    }

    public void initPlannerUsingKnownGraph() {
        try {
            File graphFile = Paths.get(Storage.GRAPH_RESOURCE.toURI()).toFile();
            Graph<Node, GraphEdge> graph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(graphFile);

            graphMaker = new GraphMaker();

            if (graph == null) {
                graphMaker.createGraphFromKnownRequests(15000);
                SerializationUtils.writeObjectToFile(graphMaker.getGraph(), graphFile);
            } else {
                graphMaker.setGraph(graph);
            }
            graphMaker.createKDTree();
            routePlanner = new RoutePlanner(graphMaker);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
