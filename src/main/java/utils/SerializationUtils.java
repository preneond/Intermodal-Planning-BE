package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import general.GraphMaker;
import model.graph.GraphEdge;
import model.planner.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.geojson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SerializationUtils {

    private static final Logger logger = LogManager.getLogger(SerializationUtils.class);


    public static void writeObjectToFile(Object serObj, File file) {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            logger.info("The object was succesfully written to a file");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public static Object readObjectFromFile(File file) {
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Object obj = objectIn.readObject();
            objectIn.close();
            logger.info("The object was red successfully from the file");
            return obj;

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }


    public static File writeGraphToGeoJSONFile(Graph<Node, GraphEdge> graph, String filePath) throws IOException {
        return GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, filePath);
    }

    public static File writeGraphToGeoJSONFile(Graph<Node, GraphEdge> graph, TransportMode mode,
                                               String filePath) throws IOException {
        return GeoJSONBuilder.getInstance().buildGeoJSONFile(graph, mode, filePath);
    }

    public static String writeGraphToGeoJSONString(Graph<Node, GraphEdge> graph) {
        return GeoJSONBuilder.getInstance().buildGeoJSONString(graph);
    }

    public static Graph readGraphFromGeoJSON(File file) {
        List<Step> stepList = new ArrayList<>();
        try {
            FileInputStream fileIn = new FileInputStream(file);
            FeatureCollection featureCollection = new ObjectMapper().readValue(fileIn, FeatureCollection.class);

            GeoJsonObject geometry;
            Step step;
            for (Feature feature : featureCollection.getFeatures()) {
                geometry = feature.getGeometry();
                if (geometry instanceof LineString) {
                    List<LngLatAlt> coords = ((LineString) geometry).getCoordinates();
                    Map<String, Object> properties = feature.getProperties();

                    step = new Step();
                    step.startLocation = lngLatAltToLocation(coords.get(0));
                    step.endLocation = lngLatAltToLocation(coords.get(1));
                    step.transportMode = (TransportMode) properties.get("mode");
                    step.distanceInMeters = (long) properties.get("distance");
                    step.durationInSeconds = (long) properties.get("duration");

                    stepList.add(step);
                }
            }
            Leg leg = new Leg();
            leg.steps = stepList;

            Route route = new Route();
            route.legList = new ArrayList<>();
            route.legList.add(leg);
            List<Route> routeList = new ArrayList<>();
            routeList.add(route);

            return GraphMaker.getInstance().createGraph(routeList);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }


    public static Location lngLatAltToLocation(LngLatAlt latLng) {
        return new Location(latLng.getLatitude(), latLng.getLongitude());
    }
}