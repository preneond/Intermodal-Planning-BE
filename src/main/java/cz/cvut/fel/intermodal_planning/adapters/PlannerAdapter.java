package cz.cvut.fel.intermodal_planning.adapters;

import cz.cvut.fel.intermodal_planning.model.planner.Location;
import cz.cvut.fel.intermodal_planning.model.planner.Route;
import cz.cvut.fel.intermodal_planning.model.planner.TransportMode;

import java.util.List;


public abstract class PlannerAdapter {

    public static final float WALKING_SPEED_MPS = 1.4f;

    public abstract List<Route> findRoutes(Location origin, Location destination, TransportMode mode);

    public abstract List<Route> findRoutes(Location origin, Location destination);

    public abstract Route findRoute(Location origin, Location destination, TransportMode mode);

    public abstract Route findRoute(Location origin, Location destination);

}
