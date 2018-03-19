package client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.umotional.basestructures.Node;
import general.Main;
import model.planner.Location;
import model.planner.TransportMode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Map;

public class OTPApiClient {
    private static OTPApiClient sharedInstance;


    private static final Logger logger = LogManager.getLogger(OTPApiClient.class);

    private static final String PLANNER_ENDPOINT = "http://127.0.0.1:8080/otp/routers/default/plan";

    public static OTPApiClient getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new OTPApiClient();
            logger.info("Creating the new instance of " + OTPApiClient.class.getName());
        }
        return sharedInstance;
    }

    public JSONObject sendNewRequest(Location origin, Location destination) {
        Client client = Client.create();
        WebResource webResource = client
                .resource(PLANNER_ENDPOINT)
                .queryParam("fromPlace", origin.toString())
                .queryParam("toPlace", destination.toString())
                .queryParam("showIntermediateStops", "true");

        return sendNewRequest(webResource);
    }

    public JSONObject sendNewRequest(Location origin, Location destination, TransportMode mode) {
        Client client = Client.create();
        WebResource webResource = client
                .resource(PLANNER_ENDPOINT)
                .queryParam("fromPlace", origin.toString())
                .queryParam("toPlace", destination.toString())
//                .queryParam("mode", mode.name())
                .queryParam("showIntermediateStops", "true");

        return sendNewRequest(webResource);

    }

    private JSONObject sendNewRequest(WebResource webResource) {
        try {
            Main.numOfRequests++;
            ClientResponse response = webResource.accept("application/json")
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
            String stringOutput = response.getEntity(String.class);

            logger.debug("Output from Server: \n" + stringOutput);

            return new JSONObject(stringOutput);

        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }
}

