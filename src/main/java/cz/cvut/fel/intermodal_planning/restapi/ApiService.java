package cz.cvut.fel.intermodal_planning.restapi;

/**
 * Created by Ondrej Prenek on 27/07/2017.
 * This code is owned by Umotional s.r.o. (IN: 03974618).
 * All Rights Reserved.
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/api")
public class ApiService {

    private final Logger log = Logger.getLogger(ApiService.class.getName());

    @GET
    @Path("/getRouteFromLatLng")
    public Response getAchievablePolygon(@QueryParam("latitude") double latitude, @QueryParam("longitude") double longitude) {

        log.info("Building a JSON response...");

        log.info("Sending response...");

        return Response.status(200).entity("").build();
    }

    @GET
    @Path("/getRouteFromLatLng")
    public Response getAchievablePolygon(@QueryParam("latitude") double latitude,
                                         @QueryParam("longitude") double longitude,
                                         @QueryParam("transportMode") String transportMode) {
        log.info("Building a JSON response...");

        log.info("Sending response...");

        return Response.status(200).entity("").build();
    }


}

