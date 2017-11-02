package adapters;

import client.GMapsApiClient;
import com.google.maps.model.*;
import model.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class GMapsPlannerAdapter extends PlannerAdapter {
    private DirectionsResult result;

    public Route findBestRoute(Address origin, Address destination, TransportMode mode, Timestamp arrival) {
        DateTime dt = getDateTime(arrival);
        TravelMode travelMode = getTravelMode(
                mode);

        if (origin.address == null || destination.address == null ) {
            LatLng originLatLng = new LatLng(origin.latitude, origin.longitude);
            LatLng destinationLatLng = new LatLng(destination.latitude, destination.longitude);
            result = GMapsApiClient.getInstance().sendNewRequest(originLatLng, destinationLatLng, travelMode, getDateTime(arrival), true);
        } else {
            result = GMapsApiClient.getInstance().sendNewRequest(origin.address, destination.address, travelMode, getDateTime(arrival), true);
        }

        try {
            return getRouteList(result).get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Route> getRouteList(DirectionsResult directions) {
        legList = new ArrayList<>();
        routeList = new ArrayList<>();
        Route tmpRoute;
        Step tmpStep;
        Leg tmpLeg;

        for (DirectionsRoute directionsRoute : directions.routes) {
            tmpRoute = new Route();
            legList = new ArrayList<>();

            for (DirectionsLeg leg : directionsRoute.legs) {
                stepList = new ArrayList<>();
                for (DirectionsStep step: leg.steps){
                    tmpStep = new Step();
                    tmpStep.distance = step.distance.inMeters;
                    tmpStep.duration = step.duration.inSeconds;
                    tmpStep.startLocation = getAddress(step.startLocation);
                    tmpStep.endLocation = getAddress(step.endLocation);
                    tmpStep.transportMode = getTransportMode(step.travelMode);

                    stepList.add(tmpStep);
                }

                tmpLeg = new Leg();
                startLocation = getAddress(leg.startLocation);
                startLocation.address = leg.startAddress;

                endLocation = getAddress(leg.endLocation);
                endLocation.address = leg.endAddress;

                tmpLeg.duration = leg.duration.inSeconds;
                tmpLeg.distance = leg.distance.inMeters;
                tmpLeg.startLocation = startLocation;
                tmpLeg.endLocation = endLocation;

                legList.add(tmpLeg);
            }
            tmpRoute.legList = legList;
            routeList.add(tmpRoute);
        }
        return routeList;
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

    private Address getAddress(String address){
        return new Address(address);
    }

    private Address getAddress(LatLng location){
        return new Address(location.lat,location.lng);
    }


    private DateTime getDateTime(Timestamp dt) {
        return new DateTime(dt, DateTimeZone.getDefault());
    }
}
