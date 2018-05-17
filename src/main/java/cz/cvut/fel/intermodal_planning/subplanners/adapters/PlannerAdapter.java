package cz.cvut.fel.intermodal_planning.subplanners.adapters;

import cz.cvut.fel.intermodal_planning.planner.model.Location;
import cz.cvut.fel.intermodal_planning.planner.model.Route;
import cz.cvut.fel.intermodal_planning.planner.model.TransportMode;

import java.util.List;


public interface PlannerAdapter {
    List<Route> findRoutes(Location origin, Location destination, TransportMode mode);

    List<Route> findRoutes(Location origin, Location destination);

    Route findRoute(Location origin, Location destination, TransportMode mode);

    Route findRoute(Location origin, Location destination);

}
