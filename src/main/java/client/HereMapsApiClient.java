package client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import model.planner.TransportMode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Map;

public class HereMapsApiClient {

    private static final Logger logger = LogManager.getLogger(HereMapsApiClient.class);

    private static final String APP_ID = "qvirmHUDyEL1HeGr7ERJ";
    private static final String APP_CODE = "4KMZH_isp8_oG9ng848ZWw";

//    private static final String TRANSIT_ENDPOINT = "https://transit.cit.api.here.com/v3/route.json";
    private static final String ROUTING_ENDPOINT = "https://route.cit.api.here.com/routing/7.2/calculateroute.json";

    public static JSONObject sendNewRequest(String origin, String destination, TransportMode mode, Timestamp departure) {
        Client client = Client.create();
        WebResource webResource = client
                    .resource(ROUTING_ENDPOINT)
                    .queryParam("app_id", APP_ID)
                    .queryParam("app_code", APP_CODE)
                    .queryParam("waypoint0", origin)
                    .queryParam("waypoint1", destination)
                    .queryParam("departure", "now")
                    .queryParam("combineChange","true")
                    .queryParam("mode","fastest;publicTransport");

        switch (mode) {
            case TRANSIT:
                webResource.queryParam("mode","fastest;publicTransport");
            case CAR:
                webResource.queryParam("mode","fastest;car");
            default:
                webResource.queryParam("mode","fastest;car");
        }

//        try {
//            ClientResponse response = webResource.accept("application/json")
//                    .get(ClientResponse.class);
//
//            if (response.getStatus() != 200) {
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + response.getStatus());
//            }
//
//            String output = response.getEntity(String.class);
//
//            logger.debug("Output from Server: \n" + output);
//
//            return response.getProperties();
//
//        } catch (Exception e) {
//            logger.error(e);
//        }
        return null;

    }

}
