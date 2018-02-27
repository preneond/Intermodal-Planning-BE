package client;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import org.joda.time.DateTime;
import general.Main;

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
//        context.setApiKey("AIzaSyBUQP_eNwggd5cJ4BAahPzM VzsL0F5vNm8");
    }

    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode, DateTime departure) {
        try {
            Main.numOfRequests++;
                return DirectionsApi.newRequest(context)
                        .origin(origin)
                        .destination(destination)
                        .mode(mode)
                        .departureTime(departure)
                        .await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode) {
        DateTime time = new DateTime(DateTime.now());
        try {
                Main.numOfRequests++;
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

