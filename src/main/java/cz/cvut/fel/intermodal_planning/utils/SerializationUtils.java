package cz.cvut.fel.intermodal_planning.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.maps.model.DirectionsResult;
import com.umotional.basestructures.Graph;
import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.planner.GraphMaker;
import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.geojson.*;
import org.json.JSONObject;

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

    public static void writeStringToFile(String text, File file) throws IOException {
        FileUtils.writeStringToFile(file, text);
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
                    step.transportMode = TransportMode.valueOf((String) properties.get("mode"));

                    System.out.println(properties.get("duration"));
                    step.durationInSeconds = ((Number) properties.get("duration")).longValue();

                    stepList.add(step);
                }
            }
            Leg leg = new Leg();
            leg.steps = stepList;

            Route route = new Route();
            route.legList.add(leg);
            List<Route> routeList = new ArrayList<>();
            routeList.add(route);
            fileIn.close();

            return new GraphMaker().createGraph(routeList);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }


    public static Location lngLatAltToLocation(LngLatAlt latLng) {
        return new Location(latLng.getLatitude(), latLng.getLongitude());
    }

    public static void writeODPairToGson(Object odPair, File file) {
        Gson gson = new Gson();
        String requestJson = gson.toJson(odPair);

        try {
            writeStringToFile(requestJson, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Location[] readODPairFromGson(File file) {
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            Location[] data = gson.fromJson(reader, Location[].class);
            reader.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static void writeRequestToGson(Object request, File file) {
        Gson gson = new Gson();
        String requestJson = gson.toJson(request);

        try {
            writeStringToFile(requestJson, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJSONObjectFromFile(File file) {
        try {
            InputStream is = new FileInputStream(file);
            String jsonTxt = IOUtils.toString(is);
            is.close();

            return new JSONObject(jsonTxt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DirectionsResult readDirectionsResultFromGson(File file) {
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            DirectionsResult data = gson.fromJson(reader, DirectionsResult.class);
            reader.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}