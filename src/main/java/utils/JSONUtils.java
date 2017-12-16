package utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.planner.Route;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class JSONUtils {

    private static final Logger log = Logger.getLogger(JSONUtils.class);
    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Convert Java object to JSON
     *
     * @param object Java object
     * @return Java object as JSON
     */
    public static String javaObjectToJSON(Object object) {
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String s = mapper.writeValueAsString(object);
            mapper.disable(SerializationFeature.INDENT_OUTPUT);
            return s;
        } catch (JsonProcessingException e) {
            log.warn("Cannot convert " + object.getClass().getSimpleName() + " to JSON string.", e);
            return null;
        }
    }

    /**
     * Convert Java object to one line JSON
     *
     * @param object Java object
     * @return Java object as JSON
     */
    public static String javaObjectToOneLineJSON(Object object) {
        // JAVA -> JSON
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Cannot convert " + object.getClass().getSimpleName() + " to JSON string.", e);
            return null;
        }
    }

    public static <T> T convertJSONStringToDesiredObject(String json, TypeReference<T> clazz) {
        // JSON -> JAVA
        T obj = null;
        try {
            obj = mapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Exception in converting json " + ((json == null) ?
                    "null" :
            json.substring(0, Math.min(json.length(), 100))) + "to JAVA class" + clazz.toString() + ". ", e);
        }
        return obj;

    }

    public static JSONObject createJSONPoint(double lat, double lon) {
        JSONObject point = new JSONObject();
        point.put("type", "Point");
        JSONArray coords = new JSONArray();
        coords.put(lon);
        coords.put(lat);
        point.put("coordinates", coords);
        return point;
    }

    public static String javaObjectToGeoJSON(List<Route> routeList) {
        return "";
    }
}