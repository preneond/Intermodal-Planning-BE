package model.planner;

import java.util.List;

public class Step {
    public Location startLocation;
    public Location endLocation;
    public long distanceInMeters;
    public long durationInSeconds;
    public TransportMode transportMode;
    public List<Step> substeps;
}
