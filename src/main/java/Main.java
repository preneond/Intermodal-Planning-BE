import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
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
            Graph<Node, GraphEdge> graph = (Graph<Node, GraphEdge>) SerializationUtils.readObjectFromFile(file);
            graphMaker.setGraph(graph);

            routePlanner.findRoute();

            SerializationUtils.writeObjectToFile(graphMaker.getGraph(), file);
            logger.info("Success");

            GeoJSONBuilder geoJSONBuilder = new GeoJSONBuilder();
            geoJSONBuilder.addPolylinesFromGraph(graphMaker.getGraph());

            geoJSONBuilder.buildJSONFile("/Users/ondrejprenek/Desktop/geo_out.json");
        } catch (IOException | URISyntaxException e) {
            logger.error("Failure");
            e.printStackTrace();
        }
    }
}
