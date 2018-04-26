package cz.cvut.fel.intermodal_planning.restapi;

/**
 * Created by Ondrej Prenek on 27/07/2017.
 * This code is owned by Umotional s.r.o. (IN: 03974618).
 * All Rights Reserved.
 */

import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import cz.cvut.fel.intermodal_planning.utils.GeoJSONBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.logging.Logger;

@Path("/api")
public class ApiService {

    private final Logger logger = Logger.getLogger(ApiService.class.getName());

    @GET
    @Path("/getIntermodalRoute")
    public Response getIntermodalRoute(@QueryParam("origin") String originStr,
                                       @QueryParam("destination") String destinationStr) {
        return getIntermodalRoute(originStr, destinationStr, "");
    }

    @GET
    @Path("/getIntermodalRouteWithMode")
    public Response getIntermodalRoute(@QueryParam("origin") String originStr,
                                       @QueryParam("destination") String destinationStr,
                                       @QueryParam("availableModes") String availableModesStr) {
        try {
            double[] originLoc = Arrays.stream(originStr.split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] destinationLoc = Arrays.stream(destinationStr.split(",")).mapToDouble(Double::parseDouble).toArray();
            TransportMode[] availableModes = availableModesStr.isEmpty() ? new TransportMode[]{} :
                    (TransportMode[]) Arrays
                            .stream(availableModesStr.split(","))
                            .map(modeStr -> TransportMode.valueOf(modeStr))
                            .toArray();

            if (originLoc.length != 2 || destinationLoc.length != 2)
                throw new IllegalArgumentException("origin or destination length is not 2");

            logger.info("Building a JSON response, args are valid...");

            PlannerInitializer plannerInitializer = PlannerInitializer.getInstance();

            Location origin = new Location(originLoc[0], originLoc[1]);
            Location destination = new Location(destinationLoc[0], destinationLoc[1]);
            Route route = plannerInitializer.routePlanner.findRoute(origin, destination, availableModes);
            String pathDesc = ResponseBuilder.buildRouteDescription(route);

            String geoJSONStr = GeoJSONBuilder.getInstance().buildGeoJSONStringForRoute(route);

            String resultJSON = "{" +
                    "\"description\":" + pathDesc + ","
                    + "\"route\":" + geoJSONStr
                    + "}";
            String responseStr = route.isEmpty() ? "Route is empty" : resultJSON;

            return Response
                    .status(200)
                    .entity(responseStr)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError().build();
        }

    }
}

