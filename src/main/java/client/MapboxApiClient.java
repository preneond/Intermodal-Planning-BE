package client;

import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.commons.models.Position;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import retrofit2.Response;

import java.io.IOException;

public class MapboxApiClient {

    private static final Logger logger = LogManager.getLogger(MapboxApiClient.class);

    private static String accessToken = "pk.eyJ1IjoicHJlbmVvbmQiLCJhIjoiY2o1ZGxlbGMxMGxicTJxcnlqMXdoYXZtciJ9.myreL4tEUijFKkauE1ysbA";
    private static MapboxDirections.Builder builder;

    public MapboxApiClient() {
        builder = new MapboxDirections.Builder().setAccessToken(accessToken);
    }

    public Response<DirectionsResponse> sendNewRequest(Position origin, Position destination) {
        try {
            return builder.setOrigin(origin)
                    .setDestination(destination)
                    .build()
                    .executeCall();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
