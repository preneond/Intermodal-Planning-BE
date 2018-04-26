package cz.cvut.fel.intermodal_planning.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.cvut.fel.intermodal_planning.general.Main;
import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import cz.cvut.fel.intermodal_planning.utils.SerializationUtils;

import java.io.File;

public class OTPApiClient {
    private static OTPApiClient sharedInstance;


    private static final Logger logger = LogManager.getLogger(OTPApiClient.class);

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
                .resource(Storage.OTP_ENDPOINT)
                .queryParam("fromPlace", origin.toString())
                .queryParam("toPlace", destination.toString())
                .queryParam("showIntermediateStops", "true");

        return sendNewRequest(webResource, TransportMode.TRANSIT);
    }

    public JSONObject sendNewRequest(Location origin, Location destination, TransportMode mode) {
        Client client = Client.create();
        WebResource webResource = client
                .resource(Storage.OTP_ENDPOINT)
                .queryParam("fromPlace", origin.toString())
                .queryParam("toPlace", destination.toString())
                .queryParam("mode", mode.name())
                .queryParam("showIntermediateStops", "true");

        return sendNewRequest(webResource, mode);

    }

    private JSONObject sendNewRequest(WebResource webResource, TransportMode mode) {
        try {
            ClientResponse response = webResource.accept("application/json")
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            logger.info("Request: " + webResource.getURI());
            String stringResponse = response.getEntity(String.class);

//            int tmpCount = (transportMode == TransportMode.TRANSIT) ? ++Storage.TRANSIT_REQUEST_COUNT : ++Storage.BIKE_REQUEST_COUNT;
//            File file = new File(Storage.OTP_REQUEST_STORAGE + transportMode.toString() + "/request_" + tmpCount + ".txt");
//            SerializationUtils.writeStringToFile(stringResponse, file);

            return new JSONObject(stringResponse);

        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public JSONObject getKnownRequest(int numOfRequest, TransportMode mode) throws NullPointerException {
        File file = new File(Storage.OTP_REQUEST_STORAGE + mode.toString() + "/request_" + numOfRequest + ".txt");

        JSONObject request = SerializationUtils.readJSONObjectFromFile(file);

        if (request == null) throw new NullPointerException("Unable to read request");

        return request;
    }
}

