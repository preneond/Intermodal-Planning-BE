package cz.cvut.fel.intermodal_planning.planner.model;

import org.geojson.LngLatAlt;

import java.io.Serializable;

/**
 * Created by Ondrej Prenek on 27/10/2017
 */
public class Location implements Serializable {
    public double lat;
    public double lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public int latE3() {
        return (int) (lat * 1E3);
    }

    public int latE6() {
        return (int) (lat * 1E6);
    }

    public int lonE3() {
        return (int) (lon * 1E3);
    }

    public int lonE6() {
        return (int) (lon * 1E6);
    }

    @Override
    public String toString() {
        return lat + ", " + lon;
    }

    public LngLatAlt toLngLatAlt() {
        return new LngLatAlt(lon, lat);
    }

    public double[] toDoubleArray() {
        return new double[]{lat, lon};
    }
}