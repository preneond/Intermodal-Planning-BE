package adapters;

import client.OTPApiClient;
import model.planner.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OpenTripPlannerAdapter extends PlannerAdapter {
    private static OpenTripPlannerAdapter sharedInstance;

    public static OpenTripPlannerAdapter getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new OpenTripPlannerAdapter();
        }
        return sharedInstance;
    }

    private OpenTripPlannerAdapter() {
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode) {
        JSONObject response = OTPApiClient.getInstance().sendNewRequest(origin, destination, mode);

        return getRouteList(response);
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination) {
        JSONObject response = OTPApiClient.getInstance().sendNewRequest(origin, destination);

        return getRouteList(response);
    }

    @Override
    public Route findRoute(Location origin, Location destination, TransportMode mode) {
        List<Route> routeList = findRoutes(origin, destination, mode);

        if (routeList.isEmpty()) return null;

//        if (routeList.get(0).legList.size() > 1) {
//            throw new RuntimeException("findLeg plan has more than one leg");
//        }

        return routeList.get(0);
    }

    @Override
    public Route findRoute(Location origin, Location destination) {
        List<Route> routeList = findRoutes(origin, destination);

        if (routeList.isEmpty()) return null;

        return routeList.get(0);
    }

    public List<Route> findRoutesFromKnownRequests(int requestNumber) {
        JSONObject jsonObject = OTPApiClient.getInstance().getKnownRequest(requestNumber);
        return getRouteList(jsonObject);
    }

    private List<Route> getRouteList(JSONObject response) {
        List<Route> routeList = new ArrayList<>();

        if (response.has("error")) return routeList;

        JSONObject obj = response.getJSONObject("plan");
        JSONArray routeArr = obj.getJSONArray("itineraries");

        JSONObject from = obj.getJSONObject("from");
        JSONObject to = obj.getJSONObject("to");

        for (Object route : routeArr) {
            Route tmpRoute = new Route();
            tmpRoute.origin = getLocation(from);
            tmpRoute.destination = getLocation(to);
            tmpRoute.legList = getLegList((JSONObject) route);

            routeList.add(tmpRoute);
        }
        return routeList;
    }

    private List<Leg> getLegList(JSONObject route) {
        List<Leg> legList = new ArrayList<>();
        JSONArray legs = route.getJSONArray("legs");
        Leg tmpLeg;

        for (Object leg : legs) {
            JSONObject jsonLeg = (JSONObject) leg;

            tmpLeg = jsonLeg.getBoolean("transitLeg") ? parseTransitLeg(jsonLeg) : parseNonTransitLeg(jsonLeg);

            legList.add(tmpLeg);
        }

        return legList;
    }

    private Leg parseNonTransitLeg(JSONObject jsonLeg) {
        Leg leg = new Leg();

        leg.transportMode = TransportMode.valueOf(jsonLeg.getString("mode"));
        leg.durationInSeconds = jsonLeg.getLong("duration");

        leg.startLocation = getLocation(jsonLeg.getJSONObject("from"));
        leg.endLocation = getLocation(jsonLeg.getJSONObject("to"));
        leg.steps = getStepList(jsonLeg.getJSONArray("intermediateStops"), leg.startLocation,
                leg.endLocation, 0, 0, false);
        leg.steps.forEach(step -> step.transportMode = TransportMode.WALK);

        return leg;
    }

    private Leg parseTransitLeg(JSONObject jsonLeg) {
        Leg leg = new Leg();
        long startTime = jsonLeg.getLong("startTime");
        long endTime = jsonLeg.getLong("endTime");

        leg.startLocation = getLocation(jsonLeg.getJSONObject("from"));
        leg.endLocation = getLocation(jsonLeg.getJSONObject("to"));
        leg.durationInSeconds = (endTime - startTime) / 1000;

        leg.steps = getStepList(jsonLeg.getJSONArray("intermediateStops"), leg.startLocation,
                leg.endLocation, startTime, endTime, true);
        leg.steps.forEach(step -> step.transportMode = TransportMode.TRANSIT);

        return leg;
    }

    private List<Step> getStepList(JSONArray steps, Location startLocation, Location endLocation,
                                   long startTime, long endTime, boolean isTransitLeg) {
        List<Step> stepList = new ArrayList<>();

        if (steps.length() == 0) return stepList;

        Step tmpStep = new Step();
        tmpStep.startLocation = startLocation;
        tmpStep.endLocation = getLocation(steps.getJSONObject(0));

        if (isTransitLeg) {
            tmpStep.durationInSeconds = steps.getJSONObject(0).getLong("arrival") - startTime;
        } else {
            tmpStep.distanceInMeters = steps.getJSONObject(0).getLong("distance");
            tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / WALKING_SPEED_MPS);
        }
        stepList.add(tmpStep);

        for (int i = 1; i < steps.length(); i++) {
            tmpStep = new Step();
            tmpStep.startLocation = stepList.get(i - 1).endLocation;
            tmpStep.endLocation = getLocation(steps.getJSONObject(i));

            if (isTransitLeg) {
                tmpStep.durationInSeconds = steps.getJSONObject(i).getLong("departure") -
                        steps.getJSONObject(i - 1).getLong("arrival");
            } else {
                tmpStep.distanceInMeters = steps.getJSONObject(i).getLong("distance");
                tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / WALKING_SPEED_MPS);
            }

            stepList.add(tmpStep);
        }

        tmpStep = new Step();
        tmpStep.startLocation = stepList.get(steps.length() - 1).endLocation;
        tmpStep.endLocation = endLocation;
        if (isTransitLeg) {
            tmpStep.durationInSeconds = endTime - steps.getJSONObject(steps.length() - 1).getLong("arrival");
        } else {
            tmpStep.distanceInMeters = steps.getJSONObject(steps.length() - 1).getLong("distance");
            tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / WALKING_SPEED_MPS);
        }
        stepList.add(tmpStep);

        return stepList;
    }

    private Location getLocation(JSONObject place) {
        double lat = place.getDouble("lat");
        double lon = place.getDouble("lon");

        return new Location(lat, lon);
    }

    private long getDuration(JSONObject leg) {
        long startTime = leg.getLong("startTime");
        long endTime = leg.getLong("endTime");

        return (endTime - startTime) / 1000;
    }
}
