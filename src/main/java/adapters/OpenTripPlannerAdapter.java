package adapters;

import client.OTPApiClient;
import model.planner.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenTripPlannerAdapter extends PlannerAdapter {
    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode, Timestamp departure) {
        return null;
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode) {
        return null;
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination, Timestamp departure) {
        return null;
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination) {
        Map<String, Object> response = OTPApiClient.getInstance().sendNewRequest(origin.toString(), destination.toString());

        return getRouteList(response);
    }

    private List<Route> getRouteList(Map<String, Object> response) {

        List<Route> routeList = new ArrayList<>();

        Map<String, Object>[] routeArr = (Map<String, Object>[]) ((Map<String, Object>) response.get("plan")).get("itineraries");


        for (Map<String, Object> route : routeArr) {
            Route tmpRoute = new Route();
            tmpRoute.legList = getLegList(route);

            routeList.add(tmpRoute);
        }


        return routeList;
    }

    private List<Leg> getLegList(Map<String, Object> route) {
        List<Leg> legList = new ArrayList<>();

        Map<String, Object>[] legs = (Map<String, Object>[]) route.get("legs");

        Leg tmpLeg;
        for (Map<String, Object> leg : legs) {
            tmpLeg = new Leg();

            tmpLeg.durationInSeconds = getDuration(leg);

            if (leg.get("distance") instanceof Long) {
                tmpLeg.distanceInMeters = (Long) leg.get("distance");
            }
            tmpLeg.startLocation = getLocation((Map<String, Object>) leg.get("from"));
            tmpLeg.endLocation = getLocation((Map<String, Object>) leg.get("to"));
            tmpLeg.steps = getStepList((Map<String, Object>[]) leg.get("steps"),startLocation,endLocation);

            legList.add(tmpLeg);
        }
        return null;
    }

    private List<Step> getStepList(Map<String, Object>[] steps, Location startLocation, Location endLocation) {
        List<Step> stepList = new ArrayList<>();

        if (steps.length == 0) return stepList;

        Step tmpStep = new Step();
        tmpStep.startLocation = startLocation;
        tmpStep.endLocation = getLocation(steps[0]);
        tmpStep.distanceInMeters = (Long) steps[0].get("distance");

        stepList.add(tmpStep);

        int i;
        for (i = 1; i < steps.length; i++) {
            tmpStep = new Step();
            tmpStep.startLocation = stepList.get(i-1).endLocation;
            tmpStep.endLocation = getLocation(steps[i]);
            tmpStep.transportMode = TransportMode.WALK;
            tmpStep.distanceInMeters = (Long) steps[i].get("distance");

            stepList.add(tmpStep);
        }

        tmpStep = new Step();
        tmpStep.startLocation = stepList.get(i-1).endLocation;
        tmpStep.endLocation = endLocation;
        tmpStep.distanceInMeters = 0;
        
        return stepList;
    }

    private Location getLocation(Map<String, Object> place) {
        double lat = (Double) place.get("lat");
        double lon = (Double) place.get("lon");

        return new Location(lat,lon);
    }

    private long getDuration(Map<String, Object> leg) {
        long startTime = (Long) leg.get("startTime");
        long endTime = (Long) leg.get("endTime");

        return (endTime - startTime) / 1000;
    }
}
