package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.planner.Leg;
import model.planner.Route;
import model.planner.Step;
import org.geojson.*;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONBuilder {

    private FeatureCollection featureCollection;

    private Feature feature;
    private GeoJsonObject geoJsonObject;
    private ObjectMapper objectMapper;

    public GeoJSONBuilder() {
        featureCollection = new FeatureCollection();
        objectMapper = new ObjectMapper();
    }


    public void addPolylines(List<Route> routes) {
        for (Route route: routes){
            List<LngLatAlt> tt = new ArrayList<>();
            for (Leg leg : route.legList){
                for (Step step: leg.steps){
                    LngLatAlt origin = new LngLatAlt(step.startLocation.lon,step.startLocation.lat);
                    LngLatAlt destination = new LngLatAlt(step.endLocation.lon,step.endLocation.lat);
                    tt.add(origin);
                    tt.add(destination);
                }
            }
            LngLatAlt[] locArr = new LngLatAlt[tt.size()];
            tt.toArray(locArr);
            addStringLine(locArr);
        }
    }

    public void addStringLine(LngLatAlt... points) {
        feature = new Feature();
        geoJsonObject = new LineString(points);

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
}

