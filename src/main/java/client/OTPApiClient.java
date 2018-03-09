package client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Map;

public class OTPApiClient {
    private static OTPApiClient sharedInstance;


    private static final Logger logger = LogManager.getLogger(OTPApiClient.class);

    private static final String PLANNER_ENDPOINT = "http://127.0.0.1:8080/otp/routers/default/plan";

    private static boolean showIntermediateStops = true;

    public static OTPApiClient getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new OTPApiClient();
            logger.info("Creating the new instance of " + OTPApiClient.class.getName());
        }
        return sharedInstance;
    }

    public JSONObject sendNewRequest(String origin, String destination) {
        Client client = Client.create();
        WebResource webResource = client
                .resource(PLANNER_ENDPOINT)
                .queryParam("fromPlace", origin)
                .queryParam("toPlace", destination)
                .queryParam("showIntermediateStops", Boolean.toString(showIntermediateStops));

        try {
            ClientResponse response = webResource.accept("application/json")
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
            String stringOutput = response.getEntity(String.class);

            logger.debug("Output from Server: \n" + stringOutput);

            JSONObject output = new JSONObject(stringOutput);
            return output;

        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }
}
