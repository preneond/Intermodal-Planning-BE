package client;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import model.planner.TransportMode;
import org.joda.time.DateTime;
import general.Main;
import utils.SerializationUtils;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class GMapsApiClient {
    public static int carRequestsCount = 35;
    public static int walkRequestsCount = 6910;
    private static GMapsApiClient sharedInstance;
    private GeoApiContext context;
    public static final String[] apiKeys = new String[]{"AIzaSyDPlXXLGlWdjqyha8M5IWJMfgM4kf_uV4A",
            "AIzaSyBAJwyMaIhPdMptdXbDcFYGzl-86J2oyKw",
            "AIzaSyB1BXePutkWKhuKzk3TTTFKvjtaDKslI5A",
            "AIzaSyDqe-iuB8J9H4zUwZ1APCYzayuOx7-oKgg"};

    public static final String REQUEST_STORAGE = "/Users/ondrejprenek/Documents/CVUT/Bachelor_thesis/Intermodal_planning/Data/requests/gmaps/";


    public static GMapsApiClient getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new GMapsApiClient();
        }
        return sharedInstance;
    }

    public GMapsApiClient() {
        context = new GeoApiContext();
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
        int idx = ThreadLocalRandom.current().nextInt(apiKeys.length);
        context.setApiKey(apiKeys[idx]);

        try {
            DirectionsResult directionResult = DirectionsApi.newRequest(context)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(time)
                    .mode(mode)
                    .await();

            Main.numOfRequests++;
            int tmpCount = mode == TravelMode.DRIVING ?  ++carRequestsCount : ++walkRequestsCount;

            File file = new File(REQUEST_STORAGE + mode.toString() + "/request_" + tmpCount+ ".txt");
            SerializationUtils.writeRequestToGson(directionResult, file);

            return directionResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DirectionsResult getKnownRequest(int count, TravelMode mode) throws NullPointerException {
        File file = new File(REQUEST_STORAGE + mode.toString() + "/request_" + count+ ".txt");

        DirectionsResult request = SerializationUtils.readDirectionsResultFromGson(file);

        if (request == null) throw new NullPointerException("Unable to read request");

        return request;
    }
}

