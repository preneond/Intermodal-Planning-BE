package adapters;

import client.GMapsApiClient;
import com.google.maps.model.*;
import model.planner.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class GMapsPlannerAdapter extends PlannerAdapter {
    private DirectionsResult result;

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode, Timestamp arrival) {
        TravelMode travelMode = getTravelMode(mode);
        LatLng originLatLng = new LatLng(origin.lat, origin.lon);
        LatLng destinationLatLng = new LatLng(destination.lat, destination.lon);

        result = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng, travelMode, getDateTime(arrival), true);

        try {
            return getRouteList(result);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode) {
        TravelMode travelMode = getTravelMode(mode);
        LatLng originLatLng = new LatLng(origin.lat, origin.lon);
        LatLng destinationLatLng = new LatLng(destination.lat, destination.lon);

        result = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng, travelMode);

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

        // Comment this block of code and uncomment code under to call only transit travel mode
        // Snippet to comment
        for(TravelMode travelMode: new TravelMode[]{TravelMode.TRANSIT, TravelMode.DRIVING, TravelMode.WALKING}) {
            tmpResult = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng,travelMode);
            result.routes = concatenate(result.routes,tmpResult.routes);
        }

// Snippet to uncomment
//        tmpResult = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng,TravelMode.TRANSIT);
//        result.routes = concatenate(result.routes,tmpResult.routes);

        try {
            return getRouteList(result);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Route findBestRoute(Location origin, Location destination, TransportMode mode, Timestamp arrival) {
        List<Route> routes = findRoutes(origin, destination, mode, arrival);

        return routes.size() == 0 ? null : routes.get(0);
    }

    @Override
    public Route findBestRoute(Location origin, Location destination, TransportMode mode) {
        List<Route> routes = findRoutes(origin, destination, mode);

        return routes.size() == 0 ? null : routes.get(0);
    }

    @Override
    public Route findBestRoute(Location origin, Location destination) {
        List<Route> routes = findRoutes(origin, destination);

        return routes.size() == 0 ? null : routes.get(0);
    }

    private List<Route> getRouteList(DirectionsResult directions) {
        legs = new ArrayList<>();
        routes = new ArrayList<>();
        Route tmpRoute;
        Step tmpStep, tmpStep2;
        Leg tmpLeg;

        for (DirectionsRoute directionsRoute : directions.routes) {
            tmpRoute = new Route();
            legs = new ArrayList<>();

            for (DirectionsLeg leg : directionsRoute.legs) {
                steps = new ArrayList<>();
                for (DirectionsStep step: leg.steps){
                    tmpStep = new Step();
                    tmpStep.distanceInMeters = step.distance.inMeters;
                    tmpStep.durationInSeconds = step.duration.inSeconds;
                    tmpStep.startLocation = getLocation(step.startLocation);
                    tmpStep.endLocation = getLocation(step.endLocation);
                    tmpStep.transportMode = getTransportMode(step.travelMode);
                    tmpStep.polyline = getPolyline(step.polyline);

                    if (step.steps != null) {
                        tmpStep.steps = new ArrayList<>();
                        for (DirectionsStep step2: step.steps){
                            tmpStep2 = new Step();
                            tmpStep2.distanceInMeters = step2.distance.inMeters;
                            tmpStep2.durationInSeconds = step2.duration.inSeconds;
                            tmpStep2.startLocation = getLocation(step2.startLocation);
                            tmpStep2.endLocation = getLocation(step2.endLocation);
                            tmpStep2.transportMode = getTransportMode(step2.travelMode);
                            tmpStep2.polyline = getPolyline(step2.polyline);
                            tmpStep.steps.add(tmpStep2);
                        }
                    }
                    steps.add(tmpStep);
                }

                tmpLeg = new Leg();
                startLocation = getLocation(leg.startLocation);

                endLocation = getLocation(leg.endLocation);

                tmpLeg.durationInSeconds = leg.duration.inSeconds;
                tmpLeg.distanceInMeters = leg.distance.inMeters;
                tmpLeg.startLocation = startLocation;
                tmpLeg.endLocation = endLocation;
                tmpLeg.steps = steps;

                legs.add(tmpLeg);
            }
            tmpRoute.legList = legs;
            routes.add(tmpRoute);
        }
        return routes;
    }

    private List<Location> getPolyline(EncodedPolyline polyline) {
        List<Location> points= new ArrayList<>();
        Location tmp;
        for(LatLng point: polyline.decodePath()){
            tmp = new Location(point.lat,point.lng);
            points.add(tmp);
        }
        return points;
    }

    private TravelMode getTravelMode(TransportMode mode) {
        switch (mode) {
            case WALK:
                return TravelMode.WALKING;
            case BIKE:
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
                return TransportMode.BIKE;
            case TRANSIT:
                return TransportMode.TRANSIT;
            case DRIVING:
                return TransportMode.CAR;
            default:
                return TransportMode.UNKNOWN;
        }
    }

    private Location getLocation(LatLng location){
        return new Location(location.lat,location.lng);
    }


    private DateTime getDateTime(Timestamp dt) {
        return new DateTime(dt, DateTimeZone.getDefault());
    }

    private  <T> T[] concatenate(T[] a, T[] b) {
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
