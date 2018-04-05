package cz.cvut.fel.intermodal_planning.adapters;

import cz.cvut.fel.intermodal_planning.client.GMapsApiClient;
import com.google.maps.model.*;
import cz.cvut.fel.intermodal_planning.model.planner.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class GMapsPlannerAdapter extends PlannerAdapter {
    private static GMapsPlannerAdapter sharedInstance;

    public static GMapsPlannerAdapter getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new GMapsPlannerAdapter();
        }
        return sharedInstance;
    }

    private GMapsPlannerAdapter() {
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode) {
        TravelMode travelMode = getTravelMode(mode);
        LatLng originLatLng = new LatLng(origin.lat, origin.lon);
        LatLng destinationLatLng = new LatLng(destination.lat, destination.lon);

        DirectionsResult result = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng, travelMode);

        try {
            return getRouteList(result);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination) {
        LatLng originLatLng = new LatLng(origin.lat, origin.lon);
        LatLng destinationLatLng = new LatLng(destination.lat, destination.lon);
        DirectionsResult result = new DirectionsResult();
        DirectionsResult tmpResult;

//        for (TravelMode travelMode : new TravelMode[]{TravelMode.DRIVING, TravelMode.WALKING}) {
//            tmpResult = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng, travelMode);

//            result.routes = concatenate(result.routes, tmpResult.routes);
        result = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng, TravelMode.DRIVING);
//        }

        try {
            return getRouteList(result);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public List<Route> findRoutesFromKnownRequests(int requestNumber, TransportMode mode) {
        DirectionsResult result = GMapsApiClient.getInstance().getKnownRequest(requestNumber, getTravelMode(mode));

        try {
            return getRouteList(result);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    @Override
    public Route findRoute(Location origin, Location destination, TransportMode mode) {
        List<Route> routeList = findRoutes(origin, destination, mode);

        if (routeList.isEmpty()) return null;

        if (routeList.get(0).legList.size() > 1) {
            throw new RuntimeException("findLeg plan has more than one leg");
        }

        return routeList.get(0);

    }

    @Override
    public Route findRoute(Location origin, Location destination) {
        List<Route> routeList = findRoutes(origin, destination);

        if (routeList.isEmpty()) return null;

        return routeList.get(0);
    }

    private List<Route> getRouteList(DirectionsResult directions) {
        List<Leg> legs;
        List<Route> routeList;
        List<Step> steps;
        Route tmpRoute;
        Step tmpStep, tmpSubstep;
        Leg tmpLeg;

        routeList = new ArrayList<>();

        for (DirectionsRoute directionsRoute : directions.routes) {
            tmpRoute = new Route();
            legs = new ArrayList<>();

            for (DirectionsLeg leg : directionsRoute.legs) {
                float trafficConstant = leg.durationInTraffic == null ? 1 :
                        leg.durationInTraffic.inSeconds / (float) leg.duration.inSeconds;
                steps = new ArrayList<>();
                for (DirectionsStep step : leg.steps) {
                    tmpStep = new Step();
                    tmpStep.distanceInMeters = step.distance.inMeters;
                    tmpStep.durationInSeconds = (long) (trafficConstant * step.duration.inSeconds);
                    tmpStep.startLocation = getLocation(step.startLocation);
                    tmpStep.endLocation = getLocation(step.endLocation);
                    tmpStep.transportMode = getTransportMode(step.travelMode);

                    if (step.steps != null) {
                        tmpStep.substeps = new ArrayList<>();
                        for (DirectionsStep step2 : step.steps) {
                            tmpSubstep = new Step();
                            tmpSubstep.distanceInMeters = step2.distance.inMeters;
                            tmpSubstep.durationInSeconds = (long) (trafficConstant * step2.duration.inSeconds);
                            tmpSubstep.startLocation = getLocation(step2.startLocation);
                            tmpSubstep.endLocation = getLocation(step2.endLocation);
                            tmpSubstep.transportMode = getTransportMode(step2.travelMode);
                            tmpStep.substeps.add(tmpSubstep);
                        }
                    }
                    steps.add(tmpStep);
                }

                tmpLeg = new Leg();

                tmpLeg.durationInSeconds = leg.durationInTraffic == null ?
                        leg.duration.inSeconds : leg.durationInTraffic.inSeconds;
                tmpLeg.distanceInMeters = leg.distance.inMeters;
                tmpLeg.startLocation = getLocation(leg.startLocation);
                tmpLeg.endLocation = getLocation(leg.endLocation);
                tmpLeg.steps = steps;

                legs.add(tmpLeg);
            }
            tmpRoute.legList = legs;
            routeList.add(tmpRoute);
        }
        return routeList;
    }

    private TravelMode getTravelMode(TransportMode mode) {
        switch (mode) {
            case WALK:
                return TravelMode.WALKING;
            case BICYCLE:
                return TravelMode.BICYCLING;
            case TRANSIT:
                return TravelMode.TRANSIT;
            case CAR:
                return TravelMode.DRIVING;
            default:
                return TravelMode.UNKNOWN;
        }
    }

    private TransportMode getTransportMode(TravelMode mode) {
        switch (mode) {
            case WALKING:
                return TransportMode.WALK;
            case BICYCLING:
                return TransportMode.BICYCLE;
            case TRANSIT:
                return TransportMode.TRANSIT;
            case DRIVING:
                return TransportMode.CAR;
            default:
                return TransportMode.UNKNOWN;
        }
    }

    private Location getLocation(LatLng location) {
        return new Location(location.lat, location.lng);
    }

    private <T> T[] concatenate(T[] a, T[] b) {
        if (b == null) return a;
        if (a == null) return b;

        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
