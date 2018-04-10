package cz.cvut.fel.intermodal_planning.client;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import cz.cvut.fel.intermodal_planning.general.Storage;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.concurrent.ThreadLocalRandom;

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

    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode, DateTime departure) {
        int idx = ThreadLocalRandom.current().nextInt(Storage.GMAPS_API_KEYS.length);
        context.setApiKey(Storage.GMAPS_API_KEYS[idx]);

        DirectionsResult directionResult = null;
        try {
            Storage.TOTAL_REQUEST_COUNT++;
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

    public DirectionsResult sendNewRequest(LatLng origin, LatLng destination, TravelMode mode) {
//        Timestamp ts = new Timestamp(1523254800000l);
//        DateTime time = new DateTime(ts);

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

            Storage.TOTAL_REQUEST_COUNT++;
            int tmpCount = mode == TravelMode.DRIVING ? ++Storage.CAR_REQUEST_COUNT : ++Storage.WALK_REQUEST_COUNT;

            File file = new File(Storage.GMAPS_REQUEST_STORAGE + mode.toString() + "/request_" + tmpCount + ".txt");
            SerializationUtils.writeRequestToGson(directionResult, file);

            return directionResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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

