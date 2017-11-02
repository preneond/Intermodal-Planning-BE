package model;

import java.sql.Timestamp;
import java.util.List;

public class Leg {
    public TransportMode mode;
    public Address startLocation,endLocation;
    public List<Address> steps;
    public Timestamp departure;
    public Timestamp arrival;
    public long distance; //in meters
    public long duration;

    public Leg() {
    }

}