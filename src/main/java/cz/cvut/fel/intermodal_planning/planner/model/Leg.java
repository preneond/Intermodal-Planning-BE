package cz.cvut.fel.intermodal_planning.planner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class Leg {
    public Location startLocation,endLocation;
    public List<Step> steps;
    public long distanceInMeters; //in meters
    public long durationInSeconds = 0;
    public TransportMode transportMode;

    public Leg() {
        steps = new ArrayList<>();
    }

    @Override
    public String toString() {
        return  "("+startLocation + "-->" + endLocation+")";
    }
}