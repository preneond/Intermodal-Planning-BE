package cz.cvut.fel.intermodal_planning.subplanners.client;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import cz.cvut.fel.intermodal_planning.general.Storage;
import org.joda.time.DateTime;
import cz.cvut.fel.intermodal_planning.general.utils.SerializationUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
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
    }


    /**
     * Retrieving stored sublanner requests
     *
     * @param origin Origin Location
     * @param destination Destination Location
     * @param mode Transport mode
     * @param departure Departure time
     *
     * @return Response
     */
    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode, DateTime departure) {
        int idx = ThreadLocalRandom.current().nextInt(Storage.GMAPS_API_KEYS.length);
        context.setApiKey(Storage.GMAPS_API_KEYS[idx]);

        DirectionsResult directionResult = null;
        try {
            directionResult = DirectionsApi.newRequest(context)
                    .origin(origin)
                    .destination(destination)
                    .mode(mode)
                    .departureTime(departure)
                    .await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directionResult;
    }

    /**
     * Subplanner request
     *
     * @param origin Origin Location
     * @param destination Destination Location
     * @param mode Transport mode
     *
     * @return Response
     */
    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode) {
        DateTime time = new DateTime(DateTime.now());
        int idx = ThreadLocalRandom.current().nextInt(Storage.GMAPS_API_KEYS.length);
        context.setApiKey(Storage.GMAPS_API_KEYS[idx]);

        try {
            DirectionsResult directionResult = DirectionsApi.newRequest(context)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(time)
                    .mode(mode)
                    .await();

            return directionResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Subplanner Stored Request
     * @param count number of requests
     * @param mode transport mode
     * @return response
     *
     * @throws NullPointerException
     */
    public DirectionsResult getKnownRequest(int count, TravelMode mode) throws NullPointerException {
        File file = new File(Storage.GMAPS_REQUEST_STORAGE + mode.toString() + "/request_" + count + ".txt");
        DirectionsResult request = SerializationUtils.readDirectionsResultFromGson(file);

        if (request == null) throw new NullPointerException("Unable to read request");

        return request;
    }

    public <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}

