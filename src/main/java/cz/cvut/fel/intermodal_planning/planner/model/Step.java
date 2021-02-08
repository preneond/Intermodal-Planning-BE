package cz.cvut.fel.intermodal_planning.planner.model;

import java.util.List;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class Step {
    public Location startLocation;
    public Location endLocation;
    public long distanceInMeters;
    public long durationInSeconds;
    public TransportMode transportMode;
    public List<Step> substeps;
}
