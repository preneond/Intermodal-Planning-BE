package general;

import com.google.maps.model.DirectionsResult;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.graph.GraphNode;
import model.planner.Location;
import model.planner.Route;
import model.planner.TransportMode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import pathfinding.kdtree.KDTree;
import utils.GeoJSONBuilder;
import utils.SerializationUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

public class Main {
    public static int numOfRequests = 0;

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static final boolean statistics = true;

    public static final String dataPath = "/Users/ondrejprenek/Documents/CVUT/Bachelor_thesis/Intermodal_planning/Data/";

    static {
        BasicConfigurator.configure();
    }

    public static void main(String[] args) {
        URL resource = RoutePlanner.class.getResource("/perfect_graph.json");
        URL metaresource = RoutePlanner.class.getResource("/meta_graph.json");

        try {
            File file = Paths.get(resource.toURI()).toFile();
            File metafile = Paths.get(metaresource.toURI()).toFile();

            Graph<GraphNode, GraphEdge> perfectGraph = (Graph<GraphNode, GraphEdge>) SerializationUtils.readObjectFromFile(file);
            Graph<GraphNode, GraphEdge> metaGraph = (Graph<GraphNode, GraphEdge>) SerializationUtils.readObjectFromFile(metafile);

            GraphMaker perfectGraphMaker = new GraphMaker();
            perfectGraphMaker.setGraph(perfectGraph, false);
            perfectGraphMaker.createKDTree();

            GraphMaker metaGraphMaker = new GraphMaker();
            metaGraphMaker.setGraph(metaGraph, false);
            metaGraphMaker.createKDTree();

            /*
            perfectGraphMaker.setAvailableModes();
            metaGraphMaker.setAvailableModes();
            */

            SerializationUtils.writeObjectToFile(perfectGraphMaker.getGraph(), file);
            SerializationUtils.writeObjectToFile(metaGraphMaker.getGraph(), metafile);


//            if (graph != null) GraphMaker.getInstance().setGraph(graph);
//            if (metaGraph != null) GraphMaker.getInstance().setGraph(metaGraph);
//            GraphMaker.getInstance().createGraph(routePlanner.getRoutesFromKnownRequest(1000));
//            GraphMaker.getInstance().createGraph(new ArrayList<>());

//            SerializationUtils.writeObjectToFile(GraphMaker.getInstance().getGraph(), file);
//            SerializationUtils.writeObjectToFile(GraphMaker.getInstance().getGraph(), metafile);


            if (statistics) {
//                doStatistics();
                doComparision(perfectGraphMaker, metaGraphMaker);
//                SerializationUtils.writeObjectToFile(GraphMaker.getInstance().getGraph(), file);
                return;
            }
            if (!statistics) return;


//            GraphMaker.getInstance().createGraph(routePlanner.expandGraph());
//            routePlanner.createKDTree();

            //
//            routePlanner.checkFunctionality(metaGraph,);

//            List<GraphEdge> graphPath = routePlanner.findRandomPath();
/////            routePlanner.getPathDescription(graphPath, "Random");

//SPECIFIC PATH
//            Location origin = new Location( 50.080894303119145, 14.524772017397451);
//            Location destination = new Location(50.09596410563516, 14.32449882350255);
//            List<GraphEdge> graphPath = routePlanner.findPath(origin, destination);

//            logger.debug("Random path origin: " +    Location.getLocation(GraphMaker.getInstance().getGraph().getNode(graphPath.get(0).fromId)));
//            logger.debug("Random path destination: " +
//                    Location.getLocation(GraphMaker.getInstance().getGraph().getNode(graphPath.get(graphPath.size()-1).toId)));

/////            List<Location> path = routePlanner.getLocationsFromEdges(graphPath);

// INFO about path origin/destination location
//            logger.debug("Founded path origin: " + path.get(0));
//            logger.debug("Founded path destination: " + path.get(path.size()-1));

/////            File pathFile = new File(dataPath + "planning/path.json");
/////            GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, path, pathFile);

/////            Route refinementRoute = routePlanner.doRefinement(graphPath);
/////            List<Location> refinementLocations = routePlanner.getLocationSequence(refinementRoute);
/////            routePlanner.getPathDescription(refinementRoute, "refinement");

// INFO about path origin/destination location
//            logger.debug("New-found path origin: " + newfoundPath.get(0));
//            logger.debug("New-found path destination: " + newfoundPath.get(newfoundPath.size()-1));

/////            File refinedPathFile = new File(dataPath + "planning/new-found_path.json");
/////            GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, refinementLocations, refinedPathFile);

// GRAPH to GEOJSON
//          List<Route> routeList = routePlanner.expandGraph();
//          GraphMaker.getInstance().createGraph(routeList);

//            for (TransportMode mode : TransportMode.values()) {
//                File tmpFile = new File(dataPath+ "planning/geo_out_" + mode.toString().toLowerCase() + ".json");
//                SerializationUtils.writeGraphToGeoJSONFile(GraphMaker.getInstance().getGraph(), mode, tmpFile);
//            }

/////            SerializationUtils.writeObjectToFile(GraphMaker.getInstance().getGraph(), file);

        } catch (Exception e) {
            logger.error("Failure");
            e.printStackTrace();
        }
    }

    private static void doComparision(GraphMaker perfectGraphMaker, GraphMaker metaGraphMaker) {
        RoutePlanner perfectPlanner = new RoutePlanner(perfectGraphMaker);
        RoutePlanner metaPlanner = new RoutePlanner(metaGraphMaker);

        perfectGraphMaker.getGraphDescription();
        metaGraphMaker.getGraphDescription();

        Location[] odPair;
        for (int i = 0; i < 100; i++) {
            comparePath(perfectPlanner, metaPlanner);
        }
    }

    //TODO: Need to modify KDTree to be able find Node with specific transport mode option
    private static void comparePath(RoutePlanner perfectPlanner, RoutePlanner metaPlanner) {
        while (true) {
            Location[] odPair = Location.generateRandomLocationsInPrague(2);
            //CAR
            List<GraphEdge> perfectCarPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.CAR, TransportMode.WALK);
            List<GraphEdge> metaCarPath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.CAR, TransportMode.WALK);
            //TRANSIT
            List<GraphEdge> perfectTransitPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.TRANSIT, TransportMode.WALK);
            List<GraphEdge> metaTransitPath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.TRANSIT, TransportMode.WALK);
            //INTERMODAL
            List<GraphEdge> perfectIntermodalPath = perfectPlanner.findPath(odPair[0], odPair[1]);
            List<GraphEdge> metaIntermodalPath = metaPlanner.findPath(odPair[0], odPair[1]);

            if (ObjectUtils.allNotNull(perfectCarPath, metaCarPath,
                    perfectTransitPath, metaTransitPath,
                    perfectIntermodalPath, metaIntermodalPath
            )) return;
        }
    }

    private static void doStatistics(GraphMaker graphMaker) {
        RoutePlanner routePlanner = new RoutePlanner(graphMaker);

//        int[] graphSize = new int[]{85};
        double sizeDeviation;

        int findingPathCount = 100;

//        for (int i = 0; i < graphSize.length; i++) {
        long[] routeDuration = new long[findingPathCount];
        long[] refinementRouteDuration = new long[findingPathCount];
        long[] deviation = new long[findingPathCount];
//            GraphMaker.getInstance().createGraph(routePlanner.expandGraph(graphSize[i]));

        for (int j = 0; j < findingPathCount; j++) {
            List<GraphEdge> graphPath = routePlanner.findRandomPath();
            routeDuration[j] = routePlanner.getDuration(graphPath);

            Route refinementRoute = routePlanner.doRefinement(graphPath);
            refinementRouteDuration[j] = routePlanner.getDuration(refinementRoute);

            deviation[j] = routeDuration[j] - refinementRouteDuration[j];
        }

        sizeDeviation = Arrays.stream(deviation).average().orElse(0);

        File file = new File(dataPath + "statistics/statistics_graph_requests" + 10000 + ".txt");
        try {
            FileWriter writer = new FileWriter(file, false);
            for (int ii = 0; ii < findingPathCount; ii++) {
                writer.write(routeDuration[ii] + " " + refinementRouteDuration[ii] + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File reqfile = new File(dataPath + "statistics/requests.txt");
        try {
            FileWriter writer = new FileWriter(reqfile, true);
//            for (int k = 0; k < graphSize.length; k++) {
            writer.write(10000 + " requests: " + sizeDeviation + "\n");
//            }
            writer.close();
        } catch (
                IOException e)

        {
            e.printStackTrace();
        }

    }
}
