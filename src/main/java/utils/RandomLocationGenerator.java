package utils;

import model.planner.Location;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLocationGenerator {

    public static RandomLocationGenerator sharedInstance;
    private static Random random;

    public static RandomLocationGenerator getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new RandomLocationGenerator();
            random = new Random();
        }
        return sharedInstance;
    }

    public Location[] generateLocationsInPrague(int count) {
        double upLat = 50.1472;
        double bottomLat = 50.0069;
        double leftLon = 14.2946;
        double rightLon = 14.5898;

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
