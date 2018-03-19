package general;

import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.Location;
import model.planner.Route;
import model.planner.TransportMode;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.GeoJSONBuilder;
import utils.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static int numOfRequests = 0;

    private static final Logger logger = LogManager.getLogger(Main.class);

    static {
        BasicConfigurator.configure();
    }

    public static void main(String[] args) {
        RoutePlanner routePlanner = new RoutePlanner();
        URL resource = RoutePlanner.class.getResource("/graph.json");
        try {
            File file = Paths.get(resource.toURI()).toFile();
            Graph<Node, GraphEdge> graph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(file);

            if (graph != null) GraphMaker.getInstance().setGraph(graph);
            GraphMaker.getInstance().createGraph(new ArrayList<>());
            routePlanner.createKDTree();

            List<GraphEdge> graphPath = routePlanner.findRandomPath();

            logger.debug("Random path origin: " +    Location.getLocation(GraphMaker.getInstance().getGraph().getNode(graphPath.get(0).fromId)));
            logger.debug("Random path destination: " +
                    Location.getLocation(GraphMaker.getInstance().getGraph().getNode(graphPath.get(graphPath.size()-1).toId)));

            List<Location> path = routePlanner.getLocationsFromEdges(graphPath);

            logger.debug("Founded path origin: " + path.get(0));
            logger.debug("Founded path destination: " + path.get(path.size()-1));

            File pathFile = new File("/Users/ondrejprenek/Desktop/path.json");
            GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, path, pathFile);

            List<Location> newfoundPath = routePlanner.doRefinement(graphPath);

            logger.debug("New-found path origin: " + newfoundPath.get(0));
            logger.debug("New-found path destination: " + newfoundPath.get(newfoundPath.size()-1));

            File refinedPathFile = new File("/Users/ondrejprenek/Desktop/new-found_path.json");
            GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, newfoundPath, refinedPathFile);

//          List<Route> routeList = routePlanner.expandGraph();
//          GraphMaker.getInstance().createGraph(routeList);

//            for (TransportMode mode : TransportMode.values()) {
//                File tmpFile = new File("/Users/ondrejprenek/Desktop/geo_out_" + mode.toString().toLowerCase() + ".json");
//                SerializationUtils.writeGraphToGeoJSONFile(GraphMaker.getInstance().getGraph(), mode, tmpFile);
//            }

            SerializationUtils.writeObjectToFile(GraphMaker.getInstance().getGraph(), file);

        } catch (Exception e) {
            logger.error("Failure");
            e.printStackTrace();
        }
    }
}
