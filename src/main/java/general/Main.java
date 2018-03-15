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


            File pathFile = new File("/Users/ondrejprenek/Desktop/path.json");

            List<GraphEdge> graphPath = routePlanner.findRandomPath();

            List<Location> path = routePlanner.getLocationsFromEdges(routePlanner.findRandomPath());

            GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, path, pathFile);

            List<Location> refinedPath = routePlanner.doRefinement(graphPath);

            GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, refinedPath, pathFile);

//          List<Route> routeList = routePlanner.expandGraph();
//          GraphMaker.getInstance().createGraph(routeList);

            for (TransportMode mode : TransportMode.values()) {
                File tmpFile = new File("/Users/ondrejprenek/Desktop/geo_out_" + mode.toString().toLowerCase() + ".json");
                SerializationUtils.writeGraphToGeoJSONFile(GraphMaker.getInstance().getGraph(), mode, tmpFile);
            }

            SerializationUtils.writeObjectToFile(GraphMaker.getInstance().getGraph(), file);

        } catch (IOException | URISyntaxException e) {
            logger.error("Failure");
            e.printStackTrace();
        }
    }
}
