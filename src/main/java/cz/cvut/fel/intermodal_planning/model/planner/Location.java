package cz.cvut.fel.intermodal_planning.model.planner;

import com.umotional.basestructures.Node;
import cz.cvut.fel.intermodal_planning.general.Main;
import org.geojson.LngLatAlt;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

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

    public double[] toDoubleArray() { return new double[]{lat,lon};}

    public static Location getLocation(Node node) {
        return new Location(node.getLatitude(), node.getLongitude());
    }

    public static Location[] generateRandomLocationsInPrague(int count) {
        double upLat, bottomLat, leftLon, rightLon;

        if (Main.EXTENDED) {
            // + 10 km radius
            upLat = 50.25;
            bottomLat = 49.94;
            leftLon = 14.15;
            rightLon = 14.69;
        } else {
            upLat = 50.1072;
            bottomLat = 50.0269;
            leftLon = 14.2946;
            rightLon = 14.55;
        }

        Location[] resultArr = new Location[count];

        double lat,lon;

        for (int i = 0; i < count; i++) {
            lat = ThreadLocalRandom.current().nextDouble(bottomLat,upLat);
            lon = ThreadLocalRandom.current().nextDouble(leftLon,rightLon);
            resultArr[i] = new Location(lat,lon);
        }
        return resultArr;
    }
}