package adapters;

import client.GMapsApiClient;
import com.google.maps.model.*;
import model.planner.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class GMapsPlannerAdapter extends PlannerAdapter {
    private DirectionsResult result;

    @Override
    public List<Route> findRoutes(Location origin, Location destination, TransportMode mode, Timestamp arrival) {
        DateTime dt = getDateTime(arrival);

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
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Route> findRoutes(Location origin, Location destination) {
        List<Route> routeList= new ArrayList<>();

        for(TransportMode transportMode: TransportMode.values()){
            routeList.addAll(findRoutes(origin,destination,transportMode));
        }
        return routeList;
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

                    if (tmpStep.transportMode == TransportMode.TRANSIT) {
                        tmpStep.steps = new ArrayList<>();
                        for (DirectionsStep step2: step.steps){
                            tmpStep2 = new Step();
                            tmpStep2.distanceInMeters = step2.distance.inMeters;
                            tmpStep2.durationInSeconds = step2.duration.inSeconds;
                            tmpStep2.startLocation = getLocation(step.startLocation);
                            tmpStep2.endLocation = getLocation(step.endLocation);
                            tmpStep2.transportMode = TransportMode.TRANSIT;
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

    private TravelMode getTravelMode(TransportMode mode) {
        TravelMode travelMode;
        switch (mode) {
            case WALK:
                travelMode = TravelMode.WALKING;
                break;
            case BIKE:
                travelMode = TravelMode.BICYCLING;
                break;
            case TRANSIT:
                travelMode = TravelMode.TRANSIT;
                break;
            case CAR:
                travelMode = TravelMode.DRIVING;
                break;
            default:
                travelMode = TravelMode.UNKNOWN;
                break;
        }
        return travelMode;
    }

    private TransportMode getTransportMode(TravelMode mode) {
        TransportMode travelMode;
        switch (mode) {
            case WALKING:
                travelMode = TransportMode.WALK;
                break;
            case BICYCLING:
                travelMode = TransportMode.BIKE;
                break;
            case TRANSIT:
                travelMode = TransportMode.TRANSIT;
                break;
            case DRIVING:
                travelMode = TransportMode.CAR;
                break;
            default:
                travelMode = TransportMode.UNKNOWN;
                break;
        }
        return travelMode;
    }

    private Location getLocation(LatLng location){
        return new Location(location.lat,location.lng);
    }


    private DateTime getDateTime(Timestamp dt) {
        return new DateTime(dt, DateTimeZone.getDefault());
    }
}
