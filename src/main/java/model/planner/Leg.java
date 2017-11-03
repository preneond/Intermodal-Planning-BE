package model.planner;

import model.Location;

import java.sql.Timestamp;
import java.util.List;

public class Leg {
    public Location startLocation,endLocation;
    public List<Step> steps;
    public Timestamp departure;
    public Timestamp arrival;
    public long distance; //in meters
    public long duration;

    public Leg() {
    }

    @Override
    public String toString() {
        String out = "distance: " + distance + ", duration: " + duration + '\n';
        return  out;
    }
}