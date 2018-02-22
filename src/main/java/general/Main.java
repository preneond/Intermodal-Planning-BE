package general;

import client.HereMapsApiClient;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
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
            Graph<Node, GraphEdge> graph = SerializationUtils.readGraphFromGeoJSON(file);

            if (graph != null) {
                GraphMaker.getInstance().setGraph(graph);
            }

            routePlanner.findRoute();

            String filePath;
            for (TransportMode mode : TransportMode.values()) {
                filePath = "/Users/ondrejprenek/Desktop/geo_out_" + mode.toString().toLowerCase() + ".json";
                SerializationUtils.writeGraphToGeoJSONFile(GraphMaker.getInstance().getGraph(), mode, filePath);
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("Failure");
            e.printStackTrace();
        }
    }
}
