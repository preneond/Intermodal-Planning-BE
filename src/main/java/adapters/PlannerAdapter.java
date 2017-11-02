package adapters;

import model.*;

import java.sql.Timestamp;
import java.util.List;


public abstract class PlannerAdapter {
    public List<Route> routeList;
    public List<Leg> legList;
    public List<Step> stepList;
    public Address startLocation, endLocation;

    public PlannerAdapter(){
    }

    abstract Route findBestRoute(Address ad1, Address ad2, TransportMode mode, Timestamp arrival);
}
