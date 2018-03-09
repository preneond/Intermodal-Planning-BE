package adapters;

import client.OTPApiClient;
import model.planner.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
        JSONObject response = OTPApiClient.getInstance().sendNewRequest(origin.toString(),
                destination.toString());

        return getRouteList(response);
    }

    private List<Route> getRouteList(JSONObject response) {
        List<Route> routeList = new ArrayList<>();

        if (response.has("error")) return routeList;

        JSONObject obj = response.getJSONObject("plan");
        JSONArray routeArr = obj.getJSONArray("itineraries");

        for (Object route : routeArr) {
            Route tmpRoute = new Route();
            tmpRoute.legList = getLegList((JSONObject) route);

            routeList.add(tmpRoute);
        }
        return routeList;
    }

    private List<Leg> getLegList(JSONObject route) {
        List<Leg> legList = new ArrayList<>();
        JSONArray legs = route.getJSONArray("legs");
        Leg tmpLeg;
        boolean isTransitLeg;

        for (Object leg : legs) {
            JSONObject jsonLeg = (JSONObject) leg;
            tmpLeg = new Leg();

            isTransitLeg = jsonLeg.getBoolean("transitLeg");

            if (!isTransitLeg) continue;

            long startTime = jsonLeg.getLong("startTime");
            long endTime = jsonLeg.getLong("endTime");

            tmpLeg.startLocation = getLocation(jsonLeg.getJSONObject("from"));
            tmpLeg.endLocation = getLocation(jsonLeg.getJSONObject("to"));
            tmpLeg.durationInSeconds = (endTime - startTime) / 1000;

            tmpLeg.steps = getStepList(jsonLeg.getJSONArray("intermediateStops"), tmpLeg.startLocation,
                    tmpLeg.endLocation, startTime, endTime);
            tmpLeg.steps.forEach(step -> step.transportMode = TransportMode.TRANSIT);

            legList.add(tmpLeg);
        }
        return legList;
    }

    private List<Step> getStepList(JSONArray steps, Location startLocation, Location endLocation,
                                   long startTime, long endTime) {
        List<Step> stepList = new ArrayList<>();

        if (steps.length() == 0) return stepList;

        Step tmpStep = new Step();
        tmpStep.startLocation = startLocation;
        tmpStep.endLocation = getLocation(steps.getJSONObject(0));
        tmpStep.durationInSeconds = steps.getJSONObject(0).getLong("arrival") - startTime;
        stepList.add(tmpStep);

        for (int i = 1; i < steps.length(); i++) {
            tmpStep = new Step();
            tmpStep.startLocation = stepList.get(i - 1).endLocation;
            tmpStep.endLocation = getLocation(steps.getJSONObject(i));
            tmpStep.durationInSeconds = steps.getJSONObject(i).getLong("departure") - steps.getJSONObject(i - 1).getLong("arrival");

            stepList.add(tmpStep);
        }

        tmpStep = new Step();
        tmpStep.startLocation = stepList.get(steps.length() - 1).endLocation;
        tmpStep.endLocation = endLocation;
        tmpStep.transportMode = TransportMode.TRANSIT;
        tmpStep.durationInSeconds = endTime - steps.getJSONObject(steps.length() - 1).getLong("arrival");

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
