package model.planner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Leg {
    public Location startLocation,endLocation;
    public List<Step> steps;
    public long distanceInMeters; //in meters
    public long durationInSeconds = 0;
    public TransportMode mode;

    public Leg() {
        steps = new ArrayList<>();
    }

    @Override
    public String toString() {
        return  "("+startLocation + "-->" + endLocation+")";
    }
}