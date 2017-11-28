package model.planner;

public class Location {
    public double lat;
    public double lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public int latE3(){
        return (int) (lat*1E3);
    }
    public int latE6(){
        return (int) (lat*1E6);
    }
    public int lonE3(){
        return (int) (lon*1E3);
    }
    public int lonE6(){
        return (int) (lon*1E6);
    }
}