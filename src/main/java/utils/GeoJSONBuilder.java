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

    private static final Logger logger = LogManager.getLogger(GeoJSONBuilder.class);


    private FeatureCollection featureCollection;

    private Feature feature;
    private GeoJsonObject geoJsonObject;
    private ObjectMapper objectMapper;

    public GeoJSONBuilder() {
        featureCollection = new FeatureCollection();
        objectMapper = new ObjectMapper();
    }


    public void addPolylinesFromRoutes(List<Route> routes) {
        for (Route route : routes) {
            List<LngLatAlt> tt = new ArrayList<>();
            for (Leg leg : route.legList) {
                addPolylinesFromSteps(leg.steps);
            }
        }
    }

    public void addPolylinesFromGraph(Graph<Node, GraphEdge> graph) {
        for (TransportMode mode: TransportMode.values()){
            addPolylinesFromGraph(graph,mode);
        }
    }

    public void addPolylinesFromGraph(Graph<Node, GraphEdge> graph, TransportMode mode) {
        featureCollection = new FeatureCollection();

        Location loc;
        List<Location> polyline;
        LngLatAlt[] lngLatAltArr;
        for (GraphEdge edge : graph.getAllEdges().stream().filter(graphEdge -> graphEdge.mode == mode).collect(Collectors.toList())) {
            polyline = edge.polyline;
            lngLatAltArr = new LngLatAlt[polyline.size()];
            for (int i = 0; i < polyline.size(); i++) {
                loc = polyline.get(i);
                lngLatAltArr[i] = new LngLatAlt(loc.lon, loc.lat);
            }
            feature = new Feature();
            geoJsonObject = new LineString(lngLatAltArr);
//
//            Node from = graph.getNode(edge.fromId);
//            Node to = graph.getNode(edge.toId);
//            LngLatAlt origin = new LngLatAlt(from.getLongitude(),from.getLatitude());
//            LngLatAlt destination = new LngLatAlt(to.getLongitude(),to.getLatitude());
//
//            feature = new Feature();
//            geoJsonObject = new LineString(origin, destination);
            feature.setGeometry(geoJsonObject);

            Color tmpColor = edge.mode.modeColor();
            feature.setProperty("stroke", toHexString(tmpColor));

            featureCollection.add(feature);
        }
    }

    public void addPolylinesFromLegs(List<Leg> legs) {
        for (Leg leg : legs) {
            addPolylinesFromSteps(leg.steps);
        }
    }

    private void addPolylinesFromSteps(List<Step> steps) {
        for (Step step : steps) {
            if (step.steps != null) {
                this.addPolylinesFromSteps(step.steps);
                return;
            }
            LngLatAlt origin = new LngLatAlt(step.startLocation.lon, step.startLocation.lat);
            LngLatAlt destination = new LngLatAlt(step.endLocation.lon, step.endLocation.lat);

            feature = new Feature();
            geoJsonObject = new LineString(origin, destination);

            Color tmpColor = step.transportMode.modeColor();
            feature.setProperty("stroke", toHexString(tmpColor));
            feature.setGeometry(geoJsonObject);
            featureCollection.add(feature);
        }
    }

    public void addStringLine(Map<String, Object> properties, LngLatAlt... points) {
        feature = new Feature();
        geoJsonObject = new LineString(points);

        if (properties != null) {
            feature.setProperties(properties);
        }
        feature.setGeometry(geoJsonObject);
        featureCollection.add(feature);
    }

    public String buildJSONString() {
        try {
            return objectMapper.writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public void buildJSONFile(String path) throws IOException {
        File file = new File(path);
        FileWriter writer = new FileWriter(file, false);
        writer.write(buildJSONString());
        writer.close();
    }

    public final static String toHexString(Color color) throws NullPointerException {
        String hexColour = Integer.toHexString(color.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }
}

