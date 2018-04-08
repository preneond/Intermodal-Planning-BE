package cz.cvut.fel.intermodal_planning.adapters;

import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;

import java.util.List;


public interface PlannerAdapter {
    List<Route> findRoutes(Location origin, Location destination, TransportMode mode);

    List<Route> findRoutes(Location origin, Location destination);

    Route findRoute(Location origin, Location destination, TransportMode mode);

    Route findRoute(Location origin, Location destination);

}
