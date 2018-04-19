package cz.cvut.fel.intermodal_planning.model.planner;

import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Main;
import org.geojson.LngLatAlt;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class Location implements Serializable {
    public double lat;
    public double lon;


    private final static double upLat = 50.1072;
    private final static double bottomLat = 50.0269;
    private final static double leftLon = 14.2946;
    private final static double rightLon = 14.55;

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

    public double[] toDoubleArray() { return new double[]{lat,lon};}

    public static Location getLocation(Node node) {
        return new Location(node.getLatitude(), node.getLongitude());
    }

    public static Location[] generateRandomLocationsInPrague(int count) {

        Location[] resultArr = new Location[count];

        double lat,lon;

        for (int i = 0; i < count; i++) {
            lat = ThreadLocalRandom.current().nextDouble(bottomLat,upLat);
            lon = ThreadLocalRandom.current().nextDouble(leftLon,rightLon);
            resultArr[i] = new Location(lat,lon);
        }
        return resultArr;
    }

    public static Location generateRandomLocationInPrague() {
        return generateRandomLocationsInPrague(1)[0];
    }

    public static Location[] generateRandomLocationsInPrague(int count, int minDistanceInMeters) {
        return null;
    }
}