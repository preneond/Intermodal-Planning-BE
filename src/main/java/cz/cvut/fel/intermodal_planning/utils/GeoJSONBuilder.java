package cz.cvut.fel.intermodal_planning.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.planner.PlannerStatistics;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.geojson.*;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeoJSONBuilder {

    private static GeoJSONBuilder sharedInstance;

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

    private void addPolyline(List<Location> path) {
        featureCollection = new FeatureCollection();
        Feature feature = new Feature();
        LngLatAlt[] pointsArr = new LngLatAlt[path.size()];

        for (int i = 0; i < path.size(); i++) {
            pointsArr[i] = path.get(i).toLngLatAlt();
        }

        GeoJsonObject polyline = new LineString(pointsArr);
        feature.setGeometry(polyline);

        featureCollection.add(feature);
    }

    private void addPolylineFromEdgeList(Graph<Node, GraphEdge> graph, List<GraphEdge> edgeList) {
        Feature feature;
        GeoJsonObject geoJsonObject;

        featureCollection = new FeatureCollection();

        for (GraphEdge edge : edgeList) {
            Node from = graph.getNode(edge.fromId);
            Node to = graph.getNode(edge.toId);

            LngLatAlt origin = new LngLatAlt(from.getLongitude(), from.getLatitude());
            LngLatAlt destination = new LngLatAlt(to.getLongitude(), to.getLatitude());

            feature = new Feature();
            geoJsonObject = new LineString(origin, destination);
            feature.setGeometry(geoJsonObject);
            feature.setProperty("color", toHexString(edge.mode.modeColor()));

            featureCollection.add(feature);
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
            feature.setProperty("mode", edge.mode.name());
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


    public String buildGeoJSONString(List<Location> path) {
        addPolyline(path);

        try {
            return objectMapper.writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public String buildGeoJSONStringForPath(Graph<Node, GraphEdge> graph, List<GraphEdge> edgeList) {
        addPolylineFromEdgeList(graph, edgeList);

        try {
            return objectMapper.writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }


    private String buildGeoJSONString(Graph<Node, GraphEdge> graph, TransportMode mode) {
        addPolylinesFromGraph(graph, mode);

        try {
            return objectMapper.writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public File buildGeoJSONFile(List<Location> path, File file) {
        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(buildGeoJSONString(path));
            writer.close();
            logger.debug("GeoJSON has been written into file successfully");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return file;
    }

    public File buildGeoJSONFile(Graph<Node, GraphEdge> graph, File file) {
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

    public File buildGeoJSONFile(Graph<Node, GraphEdge> graph, TransportMode mode, File file) {
        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(buildGeoJSONString(graph, mode));
            writer.close();
            logger.debug("GeoJSON for " + mode.toString() + " has been written into file successfully");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return file;
    }

    public final static String toHexString(Color color) throws NullPointerException {
        String hexColour = Integer.toHexString(color.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }

    public String buildPathDescription(List<GraphEdge> path) {
        String description = "";
        if (path == null || path.isEmpty()) return description;

        long curDuration = path.get(0).durationInSeconds;
        TransportMode curMode = path.get(0).mode;
        GraphEdge curEdge;
        description += "{\"legs\": [";
        for (int i = 1; i < path.size(); i++) {
            curEdge = path.get(i);
            if (curMode == curEdge.mode) {
                curDuration += curEdge.durationInSeconds;
            } else {
                description += "{";
                description += "\"duration\":" + curDuration + ",";
                description += "\"mode\":\"" + curMode + "\",";
                description += "\"color\": \"" + toHexString(curMode.modeColor()) + "\"";
                description += "},";
                curMode = curEdge.mode;
                curDuration = curEdge.durationInSeconds;

            }
        }
        description += "{";
        description += "\"duration\":" + curDuration + ",";
        description += "\"mode\":\"" + curMode + "\",";
        description += "\"color\": \"" + toHexString(curMode.modeColor()) + "\"";
        description += "}]}";
        return description;
    }
}

