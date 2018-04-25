package cz.cvut.fel.intermodal_planning.adapters;

import cz.cvut.fel.intermodal_planning.client.OTPApiClient;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.model.planner.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OTPlannerAdapter implements PlannerAdapter {
    private static OTPlannerAdapter sharedInstance;

    public static OTPlannerAdapter getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new OTPlannerAdapter();
        }
        return sharedInstance;
    }

    private OTPlannerAdapter() {
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode) {
        JSONObject response = (mode == TransportMode.TRANSIT) ? OTPApiClient.getInstance().sendNewRequest(origin, destination) :
                OTPApiClient.getInstance().sendNewRequest(origin, destination, mode);

        return getRouteList(response);
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination) {
        JSONObject response = OTPApiClient.getInstance().sendNewRequest(origin, destination);
        List<Route> routeList = getRouteList(response);

        List<Route> bikeRoutes = findRoutes(origin, destination, TransportMode.BICYCLE);
        routeList.addAll(bikeRoutes);

        return routeList;
    }

    @Override
    public Route findRoute(Location origin, Location destination, TransportMode mode) {
        List<Route> routeList = findRoutes(origin, destination, mode);

        if (routeList.isEmpty()) return null;

        return routeList.get(0);
    }

    @Override
    public Route findRoute(Location origin, Location destination) {
        List<Route> routeList = findRoutes(origin, destination);

        if (routeList.isEmpty()) return null;

        return routeList.get(0);
    }

    public List<Route> findRoutesFromKnownRequests(int requestNumber, TransportMode mode) {
        JSONObject jsonObject = OTPApiClient.getInstance().getKnownRequest(requestNumber, mode);
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

        long distance = 0;
        if (jsonLeg.has("duration")) {
            distance = jsonLeg.getLong("duration");
        }

        leg.distanceInMeters = distance;
        leg.steps = getNonTransitStepList(jsonLeg.getJSONArray("steps"), leg.startLocation,
                leg.endLocation, leg.durationInSeconds, leg.distanceInMeters, leg.transportMode);
        leg.steps.forEach(step -> step.transportMode = leg.transportMode);

        return leg;
    }

    private Leg parseTransitLeg(JSONObject jsonLeg) {
        Leg leg = new Leg();
        long startTime = jsonLeg.getLong("startTime");
        long endTime = jsonLeg.getLong("endTime");

        leg.startLocation = getLocation(jsonLeg.getJSONObject("from"));
        leg.endLocation = getLocation(jsonLeg.getJSONObject("to"));
        leg.durationInSeconds = (endTime - startTime) / 1000;
        leg.steps = getStepListForTransit(jsonLeg.getJSONArray("intermediateStops"), leg.startLocation,
                leg.endLocation, startTime, endTime);
        leg.steps.forEach(step -> step.transportMode = TransportMode.TRANSIT);

        return leg;
    }

    private List<Step> getNonTransitStepList(JSONArray steps, Location startLocation, Location endLocation,
                                             long durationInSeconds, long distanceInMeters,
                                             TransportMode transportMode) {
        switch (transportMode) {
            case BICYCLE:
                return getStepListForBike(steps, startLocation, endLocation, durationInSeconds, distanceInMeters);
            case WALK:
                return getStepListForWalking(steps, startLocation, endLocation);
            default:
                return new ArrayList<>();
        }
    }

    private List<Step> getStepListForTransit(JSONArray steps, Location startLocation, Location endLocation,
                                             long startTime, long endTime) {
        List<Step> stepList = new ArrayList<>();

        if (steps.length() == 0) return stepList;

        Step tmpStep = new Step();
        tmpStep.startLocation = startLocation;
        tmpStep.endLocation = getLocation(steps.getJSONObject(0));

        tmpStep.durationInSeconds = (steps.getJSONObject(0).getLong("arrival") - startTime) / 1000;

        stepList.add(tmpStep);

        for (int i = 1; i < steps.length(); i++) {
            tmpStep = new Step();
            tmpStep.startLocation = stepList.get(i - 1).endLocation;
            tmpStep.endLocation = getLocation(steps.getJSONObject(i));

            tmpStep.durationInSeconds = (steps.getJSONObject(i).getLong("departure") -
                    steps.getJSONObject(i - 1).getLong("arrival")) / 1000;

            stepList.add(tmpStep);
        }

        tmpStep = new Step();
        tmpStep.startLocation = stepList.get(steps.length() - 1).endLocation;
        tmpStep.endLocation = endLocation;
        tmpStep.durationInSeconds = (endTime - steps.getJSONObject(steps.length() - 1)
                .getLong("arrival")) / 1000;
        stepList.add(tmpStep);

        return stepList;
    }

    private List<Step> getStepListForWalking(JSONArray steps, Location startLocation, Location endLocation) {
        List<Step> stepList = new ArrayList<>();

        if (steps.length() == 0) return stepList;

        Step tmpStep = new Step();
        tmpStep.startLocation = startLocation;
        tmpStep.endLocation = getLocation(steps.getJSONObject(0));

        tmpStep.distanceInMeters = steps.getJSONObject(0).getLong("distance");
        tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / Storage.WALK_SPEED_MPS);
        stepList.add(tmpStep);

        for (int i = 1; i < steps.length(); i++) {
            tmpStep = new Step();
            tmpStep.startLocation = stepList.get(i - 1).endLocation;
            tmpStep.endLocation = getLocation(steps.getJSONObject(i));


            tmpStep.distanceInMeters = steps.getJSONObject(i).getLong("distance");
            tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / Storage.WALK_SPEED_MPS);

            stepList.add(tmpStep);
        }

        tmpStep = new Step();
        tmpStep.startLocation = stepList.get(steps.length() - 1).endLocation;
        tmpStep.endLocation = endLocation;

        tmpStep.distanceInMeters = steps.getJSONObject(steps.length() - 1).getLong("distance");
        tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / Storage.WALK_SPEED_MPS);
        stepList.add(tmpStep);

        return stepList;

    }

    private List<Step> getStepListForBike(JSONArray steps, Location startLocation,
                                          Location endLocation, long distanceInMeters, long durationInSeconds) {
        List<Step> stepList = new ArrayList<>();

        if (steps.length() == 0) return stepList;

        float movingSpeed = durationInSeconds / (float) distanceInMeters;

        Step tmpStep = new Step();
        tmpStep.startLocation = startLocation;
        tmpStep.endLocation = getLocation(steps.getJSONObject(0));
        tmpStep.distanceInMeters = steps.getJSONObject(0).getLong("distance");
        tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / movingSpeed);
        stepList.add(tmpStep);

        for (int i = 1; i < steps.length(); i++) {
            tmpStep = new Step();
            tmpStep.startLocation = stepList.get(i - 1).endLocation;
            tmpStep.endLocation = getLocation(steps.getJSONObject(i));
            tmpStep.distanceInMeters = steps.getJSONObject(i).getLong("distance");
            tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / movingSpeed);
            stepList.add(tmpStep);
        }

        tmpStep = new Step();
        tmpStep.startLocation = stepList.get(steps.length() - 1).endLocation;
        tmpStep.endLocation = endLocation;
        tmpStep.distanceInMeters = steps.getJSONObject(steps.length() - 1).getLong("distance");
        tmpStep.durationInSeconds = (long) (tmpStep.distanceInMeters / movingSpeed);
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
