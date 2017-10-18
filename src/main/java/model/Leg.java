package model;

import java.sql.Timestamp;
import java.util.List;

public class Leg {
    private TransportMode mode;
    private List<Coordinate> coordinates;
    private Timestamp departureTime;
    private Timestamp arrivalTime;

    public Leg(TransportMode mode, List<Coordinate> coordinates) {
        this.mode = mode;
        this.coordinates = coordinates;
    }

    public TransportMode getMode() {
        return mode;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
}