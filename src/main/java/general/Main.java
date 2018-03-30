package general;

import client.GMapsApiClient;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.Location;
import model.planner.Route;
import model.planner.TransportMode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import utils.SerializationUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static int numOfRequests = 0;

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static final boolean statistics = true;

    public static final String dataPath = "/Users/ondrejprenek/Documents/CVUT/Bachelor_thesis/Intermodal_planning/Data/";
    private static int IntermodalCount = 0;
    private static int CarCount = 0;
    private static int TransitCount = 0;
    private static int BikeCount = 0;

    static {
        BasicConfigurator.configure();
    }

    public static void main(String[] args) {
        URL resource = RoutePlanner.class.getResource("/perfect_graph.json");
        URL metaresource = RoutePlanner.class.getResource("/meta_graph.json");

        try {
            File file = Paths.get(resource.toURI()).toFile();
            File metafile = Paths.get(metaresource.toURI()).toFile();

            Graph<Node, GraphEdge> perfectGraph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(file);
            Graph<Node, GraphEdge> metaGraph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(metafile);

//            perfectGraph.getAllEdges()
//                    .stream()
//                    .filter(graphEdge -> graphEdge.mode == TransportMode.TRANSIT)
//                    .forEach(graphEdge -> {
//                        Node from = perfectGraph.getNode(graphEdge.fromId);
//                        Node to = perfectGraph.getNode(graphEdge.toId);
//                        System.out.println(from.getLongitude() + ", " + from.getLatitude()
//                                + " " + "-Duration=" + graphEdge.durationInSeconds + "->"
//                                + to.getLongitude() + ", " + to.getLatitude());
//                    });
//            if (statistics) return;

            GraphMaker perfectGraphMaker = new GraphMaker();
            GraphMaker metaGraphMaker = new GraphMaker();

            perfectGraphMaker.setGraph(perfectGraph);
            metaGraphMaker.setGraph(metaGraph);

//            RoutePlanner perfectRoutePlanner = new RoutePlanner(perfectGraphMaker);
//            RoutePlanner metaRoutePlanner = new RoutePlanner(metaGraphMaker);
//
//            perfectRoutePlanner.expandGraph(40, TransportMode.CAR);
//            perfectRoutePlanner.expandGraphFromKnownRequests(10000);
//            metaRoutePlanner.expandGraphFromKnownRequests(2000);

            perfectGraphMaker.createKDTree();
            metaGraphMaker.createKDTree();


//            SerializationUtils.writeObjectToFile(perfectGraphMaker.getGraph(), file);
//            SerializationUtils.writeObjectToFile(metaGraphMaker.getGraph(), metafile);


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

        File file = new File(dataPath + "statistics/comparision.txt");
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.println("count: car meta, car ref, transit meta, transit ref, bike meta, bike ref, intermodal meta, intermodal ref, intermodal description meta, intermodal description ref");
            printWriter.println("---------------------------------------------------------------------------------------------------------------------------------------------");

            for (int i = 0; i < 1000; i++) {
                comparePath(perfectPlanner, metaPlanner, printWriter, i + 1);
            }
            printWriter.close();
            System.out.println("Car count: " + CarCount);
            System.out.println("Transit count: " + TransitCount);
            System.out.println("Bike count: " + BikeCount);
            System.out.println("Intermodal count: " + IntermodalCount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void comparePath(RoutePlanner perfectPlanner, RoutePlanner metaPlanner, PrintWriter printWriter, int count) {
        while (true) {
            Location[] odPair = Location.generateRandomLocationsInPrague(2);
            //CAR
            List<GraphEdge> perfectCarPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.CAR);
            List<GraphEdge> metaCarPath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.CAR);
            //TRANSIT
            List<GraphEdge> perfectTransitPath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.TRANSIT, TransportMode.WALK);
            List<GraphEdge> metaTransitPath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.TRANSIT, TransportMode.WALK);

            //BIKE
            List<GraphEdge> perfectBikePath = perfectPlanner.findPath(odPair[0], odPair[1], TransportMode.BICYCLE);
            List<GraphEdge> metaBikePath = metaPlanner.findPath(odPair[0], odPair[1], TransportMode.BICYCLE);

            //INTERMODAL
            List<GraphEdge> perfectIntermodalPath = perfectPlanner.findPath(odPair[0], odPair[1]);
            List<GraphEdge> metaIntermodalPath = metaPlanner.findPath(odPair[0], odPair[1]);

            if (ObjectUtils.allNotNull(perfectCarPath, metaCarPath,
                    perfectTransitPath, metaTransitPath,
                    perfectBikePath, metaBikePath,
                    perfectIntermodalPath, metaIntermodalPath)) {

                Long carMeta = metaPlanner.getDuration(metaCarPath);
                Long carRef = perfectPlanner.getDuration(perfectCarPath);

                Long transitMeta = metaPlanner.getDuration(metaTransitPath);
                Long transitRef = perfectPlanner.getDuration(perfectTransitPath);

                Long interMeta = metaPlanner.getDuration(metaIntermodalPath);
                Long interRef = perfectPlanner.getDuration(perfectIntermodalPath);

                Long bikeMeta = metaPlanner.getDuration(metaBikePath);
                Long bikeRef = perfectPlanner.getDuration(perfectBikePath);

                String interDescriptionMeta = getIntermodalDescription(metaIntermodalPath);
                String interDescriptionRef = getIntermodalDescription(perfectIntermodalPath);

                printWriter.println(count + ": "
                        + carMeta + ", " + carRef + ", "
                        + transitMeta + ", " + transitRef + ", "
                        + bikeMeta + ", " + bikeRef + ", "
                        + interMeta + ", " + interRef + ", "
                        + interDescriptionMeta + ", "
                        + interDescriptionRef
                );
                printWriter.println();

                if (carRef < transitRef && carRef < interRef ){//&& carRef < bikeRef) {
                    CarCount++;
                } else if (interRef < transitRef && interRef < carRef ){//&& interRef < bikeRef) {
                    IntermodalCount++;
                } else if (bikeRef < transitRef && bikeRef < carRef && bikeRef < interRef) {
                    BikeCount++;
                } else {
                    TransitCount++;
                }

                return;
            }
        }
    }

    private static String getIntermodalDescription(List<GraphEdge> intermodalPath) {
        String description = "";
        if (intermodalPath == null || intermodalPath.isEmpty()) return description;


        long curDuration = intermodalPath.get(0).durationInSeconds;
        TransportMode curMode = intermodalPath.get(0).mode;
        GraphEdge curEdge;
        for (int i = 1; i < intermodalPath.size(); i += 1) {
            curEdge = intermodalPath.get(i);
            if (curMode == curEdge.mode) {
                curDuration += curEdge.durationInSeconds;
            } else {
                description += curDuration + curMode.shortcut() + "+";
                curMode = curEdge.mode;
                curDuration = curEdge.durationInSeconds;
            }
        }
        description += curDuration + curMode.shortcut();

        return description;
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
