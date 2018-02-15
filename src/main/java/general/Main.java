package general;

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
            GraphMaker graphMaker = GraphMaker.getInstance();
//            Graph<Node, GraphEdge> graph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(file);
//
//            if (graph != null) {
//                graphMaker.setGraph(graph);
//            }

            routePlanner.findRoute();

            SerializationUtils.writeObjectToFile(graphMaker.getGraph(), file);
            logger.info("Success");

//            GeoJSONBuilder geoJSONBuilder = new GeoJSONBuilder();
//            geoJSONBuilder.addPolylinesFromGraph(graphMaker.getGraph());
//            geoJSONBuilder.buildJSONFile("/Users/ondrejprenek/Desktop/geo_out.json");

            GeoJSONBuilder geoJSONBuilder;
            for (TransportMode mode : TransportMode.values()) {
                geoJSONBuilder = new GeoJSONBuilder();
                geoJSONBuilder.addPolylinesFromGraph(graphMaker.getGraph(), mode);
                geoJSONBuilder.buildJSONFile("/Users/ondrejprenek/Desktop/geo_out_" + mode.toString().toLowerCase() + ".json");
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("Failure");
            e.printStackTrace();
        }
    }
}
