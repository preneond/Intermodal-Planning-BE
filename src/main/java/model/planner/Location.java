package model.planner;

public class Location {
    public double lat;
    public int latE3;
    public double lon;
    public int lonE3;

    public Location(double lat, double lon, int latE3, int lonE3) {
        this.lat = lat;
        this.latE3 = latE3;
        this.lon = lon;
        this.lonE3 = lonE3;
    }

    public Location(double lat, double lon) {
        this(lat,lon,(int) (lat*1E3),(int) (lon*1E3));
    }
}