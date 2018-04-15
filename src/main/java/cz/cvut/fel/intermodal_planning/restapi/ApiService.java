package cz.cvut.fel.intermodal_planning.restapi;

/**
 * Created by Ondrej Prenek on 27/07/2017.
 * This code is owned by Umotional s.r.o. (IN: 03974618).
 * All Rights Reserved.
 */

import cz.cvut.fel.intermodal_planning.model.graph.GraphEdge;
import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;
import cz.cvut.fel.intermodal_planning.planner.PlannerInitializer;
import cz.cvut.fel.intermodal_planning.utils.GeoJSONBuilder;
import cz.cvut.fel.intermodal_planning.utils.LocationUtils;
import org.apache.log4j.BasicConfigurator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Path("/api")
public class ApiService {

    private final Logger logger = Logger.getLogger(ApiService.class.getName());

    @GET
    @Path("/getIntermodalRoute")
    public Response getIntermodalRoute(@QueryParam("origin") String originStr, @QueryParam("destination") String destinationStr) {
        try {
            double[] originLoc = Arrays.stream(originStr.split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] destinationLoc = Arrays.stream(destinationStr.split(",")).mapToDouble(Double::parseDouble).toArray();
            if (originLoc.length != 2 || destinationLoc.length != 2)
                throw new ParseException("origin or destination length is not 2", 0);

            logger.info("Building a JSON response, args are valid...");

            PlannerInitializer plannerInitializer = PlannerInitializer.getInstance();

            Location origin = new Location(originLoc[0], originLoc[1]);
            Location destination = new Location(destinationLoc[0], destinationLoc[1]);
            List<GraphEdge> path = plannerInitializer.perfectRoutePlanner.findPath(origin, destination);
            String responseStr = path == null ?
                    "Path is null"
                    : GeoJSONBuilder.getInstance().buildGeoJSONString(plannerInitializer.perfectRoutePlanner.getLocationsFromEdges(path, plannerInitializer.perfectGraphMaker.getGraph()));

            return Response
                    .status(200)
                    .entity(responseStr)
                    .build();
        } catch (ParseException e) {
            logger.info("Invalid input");

            return Response.serverError().build();
        }
    }
}

