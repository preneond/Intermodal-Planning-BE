package cz.cvut.fel.intermodal_planning.subplanners.client;

import cz.cvut.fel.intermodal_planning.general.Storage;
import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONObject;
import cz.cvut.fel.intermodal_planning.general.utils.SerializationUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
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

    /**
     * Subplanner Request
     *
     * @param origin Origin Location
     * @param destination Destination Location
     *
     * @return response
     */
    public JSONObject sendNewRequest(Location origin, Location destination) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(Storage.OTP_ENDPOINT)
                .queryParam("fromPlace", origin.toString())
                .queryParam("toPlace", destination.toString())
                .queryParam("showIntermediateStops", "true");

        return sendNewRequest(webTarget, TransportMode.TRANSIT);
    }

    /**
     * Subplanner Request
     *
     * @param origin Origin Location
     * @param destination Destination Location
     * @param mode Transport Mode
     *
     * @return response
     */
    public JSONObject sendNewRequest(Location origin, Location destination, TransportMode mode) {
        Client client = ClientBuilder.newClient();

        WebTarget webResource = client
                .target(Storage.OTP_ENDPOINT)
                .queryParam("fromPlace", origin.toString())
                .queryParam("toPlace", destination.toString())
                .queryParam("mode", mode.name())
                .queryParam("showIntermediateStops", "true");

        return sendNewRequest(webResource, mode);

    }

    /**
     * Retrieving stored sublanner requests
     *
     * @param numOfRequest number of request
     * @param mode transport mode
     *
     * @return Response
     *
     * @throws NullPointerException
     */
    public JSONObject getKnownRequest(int numOfRequest, TransportMode mode) throws NullPointerException {
        File file = new File(Storage.OTP_REQUEST_STORAGE + mode.toString() + "/request_" + numOfRequest + ".txt");

        JSONObject request = SerializationUtils.readJSONObjectFromFile(file);

        if (request == null) throw new NullPointerException("Unable to read request");

        return request;
    }

    private JSONObject sendNewRequest(WebTarget webTarget, TransportMode mode) {
        try {
            ClientResponse response = webTarget.request(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            logger.info("Request: " + webTarget.getUri());
            String stringResponse = response.readEntity(String.class);

//            int tmpCount = (transportMode == TransportMode.TRANSIT) ? ++Storage.TRANSIT_REQUEST_COUNT : ++Storage.BIKE_REQUEST_COUNT;
//            File file = new File(Storage.OTP_REQUEST_STORAGE + transportMode.toString() + "/request_" + tmpCount + ".txt");
//            SerializationUtils.writeStringToFile(stringResponse, file);

            return new JSONObject(stringResponse);

        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

}

