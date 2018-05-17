package cz.cvut.fel.intermodal_planning.planner.model;

import cz.cvut.fel.intermodal_planning.general.utils.LocationUtils;

import java.util.concurrent.ThreadLocalRandom;

public class LocationArea {
    public double upLat;
    public double bottomLat;
    public double leftLon;
    public double rightLon;

    public LocationArea(double upLat, double bottomLat, double leftLon, double rightLon) {
        this.upLat = upLat;
        this.bottomLat = bottomLat;
        this.leftLon = leftLon;
        this.rightLon = rightLon;
    }

    public Location[] generateRandomLocations(int count) {
        Location[] resultArr = new Location[count];

        double lat, lon;

        for (int i = 0; i < count; i++) {
            lat = ThreadLocalRandom.current().nextDouble(bottomLat, upLat);
            lon = ThreadLocalRandom.current().nextDouble(leftLon, rightLon);
            resultArr[i] = new Location(lat, lon);
        }
        return resultArr;
    }

    public Location generateRandomLocation() {
        return generateRandomLocations(1)[0];
    }


    public Location[] generateODWithMinimalDistanceBetween(int minDistanceInMeters) {
        Location[] locations = new Location[2];

        locations[0] = generateRandomLocation();
        Location locationTo = generateRandomLocation();

        while (LocationUtils.distance(locations[0], locationTo) < minDistanceInMeters) {
            locationTo = generateRandomLocation();
        }
        locations[1] = locationTo;

        return locations;
    }


    public LocationArea[][] createGrid(int gridX, int gridY) {
        LocationArea[][] grid = new LocationArea[gridX][gridY];

        double stepX = (rightLon - leftLon) / gridX;
        double stepY = (bottomLat - upLat) / gridX;

        double tmpUpLat, tmpBottomLat, tmpLeftLon, tmpRightLon;

        for (int i = 0; i < gridX; i++) {
            tmpUpLat = upLat + i * stepX;
            tmpBottomLat = upLat + (i + 1) * stepX;
            for (int j = 0; j < gridY; j++) {
                tmpLeftLon = leftLon + j * stepY;
                tmpRightLon = leftLon + (j + 1) * stepY;

                grid[i][j] = new LocationArea(tmpUpLat, tmpBottomLat, tmpLeftLon, tmpRightLon);
            }
        }
        return grid;
    }

    public boolean containsLocation(Location location) {
        double locLon = location.lon;
        double locLat = location.lat;

        return  locLon >= leftLon && locLon <= rightLon && locLat >= bottomLat && locLat <= upLat;
    }

    public void expandArea(LocationArea locationArea) {
        leftLon = locationArea.leftLon < leftLon ? locationArea.leftLon : leftLon;
        rightLon = locationArea.rightLon > rightLon ? locationArea.rightLon : rightLon;

        upLat = locationArea.upLat < upLat ? locationArea.upLat : upLat;
        bottomLat = locationArea.bottomLat < bottomLat ? locationArea.bottomLat : bottomLat;
    }
}
