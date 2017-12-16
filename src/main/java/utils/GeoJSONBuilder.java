package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.planner.Leg;
import model.planner.Route;
import model.planner.Step;
import model.planner.TransportMode;
import org.geojson.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoJSONBuilder {

    private FeatureCollection featureCollection;

    private Feature feature;
    private GeoJsonObject geoJsonObject;
    private ObjectMapper objectMapper;

    public GeoJSONBuilder() {
        featureCollection = new FeatureCollection();
        objectMapper = new ObjectMapper();
    }


    public void addPolylinesFromRoutes(List<Route> routes) {
        for (Route route: routes){
            List<LngLatAlt> tt = new ArrayList<>();
            for (Leg leg : route.legList) {
                addPolylinesFromSteps(leg.steps);
            }
        }
    }

    public void addPolylinesFromLegs(List<Leg> legs){
        for (Leg leg : legs){
            addPolylinesFromSteps(leg.steps);
        }
    }

    private void addPolylinesFromSteps(List<Step> steps) {
        List<LngLatAlt> tt = new ArrayList<>();
        for (Step step: steps){
            if (step.steps != null){
                this.addPolylinesFromSteps(step.steps);
                return;
            }
            LngLatAlt origin = new LngLatAlt(step.startLocation.lon,step.startLocation.lat);
            LngLatAlt destination = new LngLatAlt(step.endLocation.lon,step.endLocation.lat);
            tt.add(origin);
            tt.add(destination);
        }
        Map<String,Object> properties = new HashMap<>();
        if (steps.size() > 0){
            Color tmpColor = steps.get(0).transportMode.modeColor();
            properties.put("stroke", toHexString(tmpColor));
        }

        LngLatAlt[] locArr = new LngLatAlt[tt.size()];
        tt.toArray(locArr);
        addStringLine(properties,locArr);
    }

    public void addStringLine(Map<String,Object> properties, LngLatAlt... points) {
        feature = new Feature();
        geoJsonObject = new LineString(points);

        if (properties != null){
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

    public final static String toHexString(Color colour) throws NullPointerException {
        String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }
}

