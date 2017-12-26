package client;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;


public class GMapsApiClient {
    private static GMapsApiClient sharedInstance;
    private GeoApiContext context;

    public static GMapsApiClient getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new GMapsApiClient();
        }
        return sharedInstance;
    }

    public GMapsApiClient() {
        context = new GeoApiContext();
        context.setApiKey("AIzaSyAS0eVnzQVZ9CNjqpAmgZclBZZIv7PKVno");
//        context.setApiKey("AIzaSyDvTzHucpZzqYZbSWztuFZ9pZ8SNPjArq8");
//        context.setApiKey("AIzaSyBUQP_eNwggd5cJ4BAahPzMVzsL0F5vNm8");
    }

    @SuppressWarnings("Duplicates")
    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode, DateTime dt, Boolean isArrival) {
        try {
            if (isArrival) {
                return DirectionsApi.newRequest(context)
                        .origin(origin)
                        .destination(destination)
                        .mode(mode)
                        .arrivalTime(dt)
                        .await();
            } else {
                return DirectionsApi.newRequest(context)
                        .origin(origin)
                        .destination(destination)
                        .mode(mode)
                        .departureTime(dt)
                        .await();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode) {
        DateTime time = new DateTime(DateTime.now()).plusMinutes(20);
        try {
                return DirectionsApi.newRequest(context)
                        .origin(origin)
                        .destination(destination)
                        .departureTime(time)
                        .mode(mode)
                        .await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

