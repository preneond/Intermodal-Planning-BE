package model.planner;

import java.sql.Timestamp;
import java.util.List;

public class Leg {
    public Location startLocation,endLocation;
    public List<Step> steps;
    public Timestamp departure;
    public Timestamp arrival;
    public long distanceInMeters; //in meters
    public long durationInSeconds;

    public Leg() {
    }

    @Override
    public String toString() {
        return  "("+startLocation + "-->" + endLocation+")";
    }
}