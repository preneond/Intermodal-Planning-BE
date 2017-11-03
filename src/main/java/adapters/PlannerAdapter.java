package adapters;

import model.*;
import model.planner.Leg;
import model.planner.Route;
import model.planner.Step;
import model.planner.TransportMode;

import java.sql.Timestamp;
import java.util.List;


public abstract class PlannerAdapter {
    public List<Route> routes;
    public List<Leg> legs;
    public List<Step> steps;
    public Location startLocation, endLocation;

    public PlannerAdapter(){
    }

    public abstract List<Route> findRoutes(Location origin, Location destination, TransportMode mode, Timestamp arrival);

    public abstract List<Route> findRoutes(Location origin, Location destination, TransportMode mode);

    public abstract List<Route> findRoutes(Location origin, Location destination);

    public abstract Route findBestRoute(Location origin, Location destination, TransportMode mode, Timestamp arrival);

    public abstract Route findBestRoute(Location origin, Location destination, TransportMode mode);

    public abstract Route findBestRoute(Location origin, Location destination);


}
