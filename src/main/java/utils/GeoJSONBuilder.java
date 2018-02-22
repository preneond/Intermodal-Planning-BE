package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import model.graph.GraphEdge;
import model.planner.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.geojson.*;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeoJSONBuilder {

    public static GeoJSONBuilder sharedInstance;

    private static final Logger logger = LogManager.getLogger(GeoJSONBuilder.class);

    private FeatureCollection featureCollection;
    private ObjectMapper objectMapper;

    private GeoJSONBuilder() {
        featureCollection = new FeatureCollection();
        objectMapper = new ObjectMapper();
    }

    public static GeoJSONBuilder getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new GeoJSONBuilder();
        }
        return sharedInstance;
    }

    private void addPolylinesFromGraph(Graph<Node, GraphEdge> graph) {
        featureCollection = new FeatureCollection();

        for (TransportMode mode : TransportMode.values()) {
            addPolylinesFromGraph(graph, mode);
        }
    }

    private void addPolylinesFromGraph(Graph<Node, GraphEdge> graph, TransportMode mode) {
        Feature feature;
        GeoJsonObject geoJsonObject;

        for (GraphEdge edge : graph.getAllEdges().stream().filter(graphEdge -> graphEdge.mode == mode).collect(Collectors.toList())) {
            Node from = graph.getNode(edge.fromId);
            Node to = graph.getNode(edge.toId);
            LngLatAlt origin = new LngLatAlt(from.getLongitude(), from.getLatitude());
            LngLatAlt destination = new LngLatAlt(to.getLongitude(), to.getLatitude());

            feature = new Feature();
            geoJsonObject = new LineString(origin, destination);

            feature.setGeometry(geoJsonObject);
            feature.setProperty("mode", edge.mode);
            feature.setProperty("length", edge.length);
            feature.setProperty("duration", edge.durationInSeconds);

            featureCollection.add(feature);
        }
    }

    public String buildGeoJSONString(Graph<Node, GraphEdge> graph) {
        addPolylinesFromGraph(graph);

        try {
            return objectMapper.writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public String buildGeoJSONString(Graph<Node, GraphEdge> graph, TransportMode mode) {
        addPolylinesFromGraph(graph, mode);

        try {
            return objectMapper.writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public File buildGeoJSONFile(Graph<Node, GraphEdge> graph, String filePath) throws IOException{
        File file = new File(filePath);

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(buildGeoJSONString(graph));
            writer.close();
            logger.debug("GeoJSON has been written into file successfully");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return file;
    }

    public File buildGeoJSONFile(Graph<Node, GraphEdge> graph, TransportMode mode, String filePath) throws IOException {
        File file = new File(filePath);
        FileWriter writer = new FileWriter(file, false);
        writer.write(buildGeoJSONString(graph, mode));
        writer.close();
        logger.debug("GeoJSON for " + mode.toString() + " has been written into file successfully");

        return file;
    }
}

