package cz.cvut.fel.intermodal_planning.subplanners.adapters;

import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.Route;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;

import java.util.List;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public interface PlannerAdapter {

    /**
     * Finding Route
     *
     * @param origin origin location
     * @param destination destination location
     * @param mode transport mode
     * @return routeList
     */
    List<Route> findRoutes(Location origin, Location destination, TransportMode mode);


    /**
     * Finding Route
     *
     * @param origin
     * @param destination
     * @return routeList
     */
    List<Route> findRoutes(Location origin, Location destination);


    /**
     * Finding Route
     *
     * @param origin origin location
     * @param destination destination location
     * @param mode transport mode
     * @return route
     */
    Route findRoute(Location origin, Location destination, TransportMode mode);

    /**
     * Finding Route
     *
     * @param origin origin location
     * @param destination destination location
     * @return route
     */
    Route findRoute(Location origin, Location destination);

}
