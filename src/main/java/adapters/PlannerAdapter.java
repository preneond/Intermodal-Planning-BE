package adapters;

import model.planner.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;


public abstract class PlannerAdapter {
    public List<Route> routes;
    public List<Leg> legs;
    public List<Step> steps;
    public Location startLocation, endLocation;

    public PlannerAdapter(){
    }

    public abstract List<Route> findRoutes(Location origin, Location destination, TransportMode mode, Timestamp departure);

    public abstract List<Route> findRoutes(Location origin, Location destination, TransportMode mode);

    public abstract List<Route> findRoutes(Location origin, Location destination, Timestamp departure);

    public abstract List<Route> findRoutes(Location origin, Location destination);


}
