package model.planner;

import model.Location;

public class Step {
    public Location startLocation,endLocation;
    // in meters
    public long distance;
    // in seconds
    public long duration;
    public TransportMode transportMode;
}
