import adapters.GoogleMapsRouteAdapter;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.Timestamp;

public class Main {
    public static void main(String[] args) {
        // + 1 hour
        int delay = 3600000;
        Timestamp departureTimestamp = new Timestamp(System.currentTimeMillis() + delay);
        DateTime dt = new DateTime(departureTimestamp, DateTimeZone.getDefault());

        GoogleMapsRouteAdapter adapter = new GoogleMapsRouteAdapter();
        String origin = "75 9th Ave New York, NY";
        String destination = "80 9th Ave New York, NY";
        TravelMode mode = TravelMode.TRANSIT;

        DirectionsResult directionsResult = adapter.sendNewRequest(origin, destination, mode, dt);

        for (DirectionsRoute route : directionsResult.routes) {
            System.out.println(route.summary);
        }
    }
}
