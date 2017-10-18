package adapters;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import model.Coordinate;
import model.Leg;
import org.joda.time.DateTime;

import java.util.List;


public class GoogleMapsRouteAdapter extends RouteAdapter {
    private GeoApiContext context;

    public GoogleMapsRouteAdapter(GeoApiContext context) {
        this.context = context;
    }

    public GoogleMapsRouteAdapter() {
        context = new GeoApiContext();
        context.setApiKey("AIzaSyAS0eVnzQVZ9CNjqpAmgZclBZZIv7PKVno");
    }

    private List<Leg> getLegList(DirectionsRoute directionsRoute) {
        return null;
    }

    private Coordinate getStartPoint(DirectionsRoute directionsRoute) {
        return null;
    }

    private Coordinate getEndPoint(DirectionsRoute directionsRoute) {
        return null;
    }

    private void setRoute(DirectionsRoute directionsRoute) {
        legList = getLegList(directionsRoute);
        startPoint = getStartPoint(directionsRoute);
        endPoint = getEndPoint(directionsRoute);
    }

    public DirectionsResult sendNewRequest(String origin, String destination, TravelMode mode, DateTime dt) {
        try {
            return DirectionsApi.newRequest(context)
                    .origin(origin)
                    .destination(destination)
                    .mode(mode)
                    .arrivalTime(dt)
                    .await();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
